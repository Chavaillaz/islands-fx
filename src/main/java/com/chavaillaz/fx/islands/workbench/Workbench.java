package com.chavaillaz.fx.islands.workbench;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Subscription;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.action.ActionMenus;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.internal.Texts;

/**
 * The layout of the main window: the main toolbar along the top, the status bar along the bottom, the stripes of tool
 * window buttons along the sides, and the islands in between: the content in the middle, the tool windows docked
 * beside it, and the one docked under it.
 * <p>
 * The workbench is the scene of its window ({@link #createScene(Stage)}), its main toolbar standing in the title bar.
 * Its main menu stands in the menu bar of macOS, and elsewhere in a menu opened by the first button of the main
 * toolbar, unless the application keeps it there on macOS as well ({@link MainMenuPlacement#MAIN_TOOLBAR}).
 * <p>
 * The workbench knows the actions it shows, those of its tool windows and of its main menu, and those the application
 * {@link #registerActions(Action...) registers}: their shortcuts work wherever the focus stands in the window, and the
 * {@link #getSearchAction() search of the actions} finds them.
 * <p>
 * It carries the style class {@code workbench}, the island of the content {@code content-island}, and those of the
 * tool windows {@code tool-window}. The islands carry the pseudo-classes {@code :left-stripe} and
 * {@code :right-stripe} while the stripe on that side shows, the stylesheets spacing them from the edges of the window
 * where no stripe does.
 */
public final class Workbench {

    private static final PseudoClass LEFT_STRIPE = PseudoClass.getPseudoClass("left-stripe");
    private static final PseudoClass RIGHT_STRIPE = PseudoClass.getPseudoClass("right-stripe");

    private final boolean systemMenuBar;
    private final StackPane root = new StackPane();
    private final BorderPane layout = new BorderPane();
    private final MainToolbar mainToolbar = new MainToolbar();
    private final StatusBar statusBar = new StatusBar();
    private final Island content = new Island();
    private final IslandsPane islands = new IslandsPane(content);
    private final ToolWindowStripe leftStripe = new ToolWindowStripe(ToolWindowAnchor.LEFT);
    private final ToolWindowStripe rightStripe = new ToolWindowStripe(ToolWindowAnchor.RIGHT);
    private final List<ToolWindow> toolWindows = new ArrayList<>();
    private final Map<ToolWindow, Subscription> dockings = new HashMap<>();
    private final List<Action> actions = new ArrayList<>();
    private final Map<Action, Runnable> performers = new HashMap<>();
    private final List<ActionSearch.Entry> menuEntries = new ArrayList<>();
    private final Map<KeyCombination, Runnable> shortcuts = new LinkedHashMap<>();
    private final Set<KeyCombination> systemMenuShortcuts = new HashSet<>();
    private final NotificationLayer notifications = new NotificationLayer();
    private final ActionSearch search = new ActionSearch(this::searchEntries);
    private final Action searchAction = Action.builder("workbench.searchActions", Texts.get("search.action"))
            .icon(Icons.SEARCH)
            .accelerator(KeyCombination.keyCombination("Shortcut+Shift+A"))
            .handler(search::open)
            .build();

    /**
     * Creates a workbench without content nor tool window yet, its main menu standing in the menu bar of the system
     * on macOS only.
     */
    public Workbench() {
        this(MainMenuPlacement.SYSTEM);
    }

    /**
     * Creates a workbench without content nor tool window yet, its main menu standing where told.
     *
     * @param placement where the main menu stands, such as in the main toolbar on every operating system
     */
    public Workbench(MainMenuPlacement placement) {
        this(requireNonNull(placement, "placement") == MainMenuPlacement.SYSTEM
                && System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("mac"));
    }

    /**
     * Creates a workbench without content nor tool window yet.
     *
     * @param systemMenuBar whether the main menu stands in the menu bar of the system, rather than the main toolbar
     */
    Workbench(boolean systemMenuBar) {
        this.systemMenuBar = systemMenuBar;
        root.getStyleClass().add("workbench");
        root.getChildren().addAll(layout, notifications, search);
        content.getStyleClass().add("content-island");
        layout.setTop(mainToolbar.getNode());
        layout.setLeft(leftStripe);
        layout.setCenter(islands);
        layout.setRight(rightStripe);
        layout.setBottom(statusBar.getNode());
        leftStripe.visibleProperty().subscribe(shown -> islands.pseudoClassStateChanged(LEFT_STRIPE, shown));
        rightStripe.visibleProperty().subscribe(shown -> islands.pseudoClassStateChanged(RIGHT_STRIPE, shown));
        root.sceneProperty().subscribe(scene -> {
            if (scene != null) {
                scene.getAccelerators().putAll(shortcuts);
            }
        });
        registerActions(searchAction);
    }

    /**
     * Gives the node showing the workbench, the root of the scene of its window.
     *
     * @return the root of the workbench
     */
    public Region getNode() {
        return root;
    }

    /**
     * Gives the toolbar along the top of the window.
     *
     * @return the main toolbar
     */
    public MainToolbar getMainToolbar() {
        return mainToolbar;
    }

    /**
     * Gives the bar along the bottom of the window.
     *
     * @return the status bar
     */
    public StatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Shows a content in the island in the middle, beside the tool windows.
     *
     * @param node the content, filling the island
     */
    public void setContent(Node node) {
        content.setContent(node);
    }

    /**
     * Docks a tool window on its side, after those already docked there, and adds its button to the stripe of that
     * side, or at the bottom of the left stripe for a tool window docked at the bottom, which is hidden when another one
     * shows there already.
     *
     * @param toolWindow the tool window
     * @throws IllegalArgumentException when a tool window of the same identifier is already docked
     */
    public void addToolWindow(ToolWindow toolWindow) {
        if (toolWindows.stream().anyMatch(each -> each.getId().equals(toolWindow.getId()))) {
            throw new IllegalArgumentException("A tool window named " + toolWindow.getId() + " is already docked");
        }
        Subscription docking = toolWindow.getAnchor() == ToolWindowAnchor.BOTTOM
                ? showAloneAtBottom(toolWindow)
                : Subscription.EMPTY;
        toolWindows.add(toolWindow);
        registerActions(toolWindow.getToggleAction());
        toolWindow.getActions().forEach(this::registerActions);
        dockings.put(toolWindow, docking.and(dockIsland(toolWindow)));
        stripe(toolWindow).add(toolWindow);
    }

    /**
     * Undocks a tool window: its island and its button leave the workbench, and the shortcuts of its actions stop
     * working. The items the application put in its menus for it stay, the application removing them itself. The tool
     * window can be docked again.
     *
     * @param toolWindow the tool window
     * @return {@code true} when it was docked
     */
    public boolean removeToolWindow(ToolWindow toolWindow) {
        if (!toolWindows.remove(toolWindow)) {
            return false;
        }
        dockings.remove(toolWindow).unsubscribe();
        unregisterActions(toolWindow.getToggleAction());
        toolWindow.getActions().forEach(this::unregisterActions);
        islands.undock(toolWindow);
        stripe(toolWindow).remove(toolWindow);
        return true;
    }

    /**
     * Gives the tool window docked under an identifier.
     *
     * @param id the identifier of the tool window
     * @return the tool window, or {@code null} when none of that identifier is docked
     */
    public @Nullable ToolWindow getToolWindow(String id) {
        return toolWindows.stream().filter(each -> each.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Gives the tool windows docked.
     *
     * @return the unmodifiable list of the tool windows, in the order they were docked
     */
    public List<ToolWindow> getToolWindows() {
        return Collections.unmodifiableList(toolWindows);
    }

    /**
     * Registers actions the workbench does not show by itself, such as those of the toolbars and the headers of the
     * application: their shortcuts work wherever the focus stands in the window, and the search of the actions finds
     * them. An action registered already, and a shortcut the workbench knows already, are left as they are.
     *
     * @param actions the actions
     */
    public void registerActions(Action... actions) {
        for (Action action : actions) {
            if (!this.actions.contains(action)) {
                Runnable performer = action::perform;
                this.actions.add(action);
                performers.put(action, performer);
                addShortcut(action.getAccelerator(), performer);
            }
        }
    }

    /**
     * Forgets actions registered before, such as those of a view the application closed: their shortcuts stop
     * working, and the search of the actions no longer finds them, unless they stand in the main menu.
     *
     * @param actions the actions
     */
    public void unregisterActions(Action... actions) {
        for (Action action : actions) {
            Runnable performer = performers.remove(action);
            this.actions.remove(action);
            KeyCombination accelerator = action.getAccelerator();
            if (performer != null && accelerator != null && shortcuts.get(accelerator) == performer) {
                removeShortcut(accelerator);
            }
        }
    }

    /**
     * Shows a notification in a balloon over the bottom right corner of the window, under the notifications showing,
     * until it goes away by itself or is closed.
     *
     * @param notification the notification
     * @return the subscription, which closes the notification once unsubscribed
     */
    public Subscription showNotification(Notification notification) {
        return notifications.show(notification);
    }

    /**
     * Gives the action opening the search of the actions, whose shortcut is {@code Shortcut+Shift+A}: a field over the
     * window, listing the actions the workbench knows whose text, menu or description holds the words typed, and
     * performing the one chosen. An application can show it in a menu or a toolbar as well.
     *
     * @return the action
     */
    public Action getSearchAction() {
        return searchAction;
    }

    /**
     * Tells whether the main menu stands in the menu bar of the system, whose menu of the application then holds the
     * item quitting it, rather than the main toolbar.
     *
     * @return {@code true} on macOS, unless the main menu was placed in the main toolbar
     */
    public boolean hasSystemMenuBar() {
        return systemMenuBar;
    }

    /**
     * Gives the window its main menu, once: in the menu bar of the system where it {@link #hasSystemMenuBar() stands
     * there}, otherwise in a menu opened by a button heading the main toolbar, whose shortcuts are then given to the
     * scene, so that they work while the menu is closed. The search of the actions finds the items of the menus, under
     * the path of their menus.
     *
     * @param menus the menus, in the order of the menu bar
     */
    public void setMainMenu(List<Menu> menus) {
        addMenuEntries(menus, "");
        if (systemMenuBar) {
            MenuBar menuBar = new MenuBar();
            menuBar.getMenus().setAll(menus);
            menuBar.setUseSystemMenuBar(true);
            // Laid out nowhere: the node only carries the menus to the menu bar of the system
            menuBar.setManaged(false);
            root.getChildren().add(menuBar);
        } else {
            ContextMenu menu = new ContextMenu();
            menu.getItems().setAll(menus);
            mainToolbar.getLeftItems().addFirst(
                    ActionButtons.createMenuButton(Texts.get("workbench.mainMenu"), Icons.MENU, menu,
                            MainToolbar.ICON_SIZE));
        }
    }

    /**
     * Makes the workbench the scene of a stage, its main toolbar standing in the title bar: the stage takes the
     * {@link StageStyle#EXTENDED extended} style, and the buttons of the operating system the height of the main
     * toolbar. The stage is not shown, so that the theme can be applied to the scene before.
     *
     * @param stage the stage, not shown yet, whose style is set
     * @return the scene showing the workbench
     */
    public Scene createScene(Stage stage) {
        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.EXTENDED);
        HeaderBar.setSystemButtonHeight(stage, MainToolbar.HEIGHT);
        stage.setScene(scene);
        return scene;
    }

    /**
     * Makes a tool window docked at the bottom the only one showing there: hidden when docked while another one shows,
     * and hiding the others once shown.
     *
     * @return the subscription, which stops hiding the others once unsubscribed
     */
    private Subscription showAloneAtBottom(ToolWindow toolWindow) {
        if (toolWindow.isVisible() && bottomToolWindows().anyMatch(ToolWindow::isVisible)) {
            toolWindow.setVisible(false);
        }
        return toolWindow.visibleProperty().subscribe(shown -> {
            if (shown) {
                bottomToolWindows().filter(other -> other != toolWindow).forEach(other -> other.setVisible(false));
            }
        });
    }

    private ToolWindowStripe stripe(ToolWindow toolWindow) {
        return toolWindow.getAnchor() == ToolWindowAnchor.RIGHT ? rightStripe : leftStripe;
    }

    private Stream<ToolWindow> bottomToolWindows() {
        return toolWindows.stream().filter(each -> each.getAnchor() == ToolWindowAnchor.BOTTOM);
    }

    /**
     * Gives the entries of the search for the actions the workbench knows: those of its main menu under the path of
     * their menus, then the others, the action opening the search left out.
     *
     * @return the entries, each action once
     */
    List<ActionSearch.Entry> searchEntries() {
        Map<Action, ActionSearch.Entry> entries = new LinkedHashMap<>();
        menuEntries.forEach(entry -> entries.putIfAbsent(entry.action(), entry));
        actions.forEach(action -> entries.putIfAbsent(action, new ActionSearch.Entry(action, "")));
        entries.remove(searchAction);
        return List.copyOf(entries.values());
    }

    /**
     * Gives the layer of the notifications, for the tests to look into.
     */
    NotificationLayer getNotifications() {
        return notifications;
    }

    /**
     * Gives the search of the actions, for the tests to look into.
     */
    ActionSearch getSearch() {
        return search;
    }

    /**
     * Makes the items of menus found by the search, and their shortcuts known: handled by the menu bar of the system
     * where the main menu stands in it, otherwise firing the item from the scene, whatever action held the shortcut.
     */
    private void addMenuEntries(List<? extends MenuItem> items, String path) {
        for (MenuItem item : items) {
            String text = requireNonNullElse(item.getText(), "");
            if (item instanceof Menu menu) {
                addMenuEntries(menu.getItems(), path.isEmpty() ? text : path + " › " + text);
            } else if (!(item instanceof SeparatorMenuItem) && !text.isEmpty()) {
                Action action = ActionMenus.getAction(item);
                menuEntries.add(new ActionSearch.Entry(action == null ? wrap(item, path) : action, path));
                KeyCombination accelerator = item.getAccelerator();
                if (accelerator != null && systemMenuBar) {
                    systemMenuShortcuts.add(accelerator);
                    removeShortcut(accelerator);
                } else if (accelerator != null) {
                    removeShortcut(accelerator);
                    addShortcut(accelerator, item::fire);
                }
            }
        }
    }

    private void addShortcut(@Nullable KeyCombination accelerator, Runnable runnable) {
        if (accelerator != null && !systemMenuShortcuts.contains(accelerator) && !shortcuts.containsKey(accelerator)) {
            shortcuts.put(accelerator, runnable);
            Scene scene = root.getScene();
            if (scene != null) {
                scene.getAccelerators().put(accelerator, runnable);
            }
        }
    }

    private void removeShortcut(KeyCombination accelerator) {
        Runnable runnable = shortcuts.remove(accelerator);
        Scene scene = root.getScene();
        if (runnable != null && scene != null) {
            scene.getAccelerators().remove(accelerator, runnable);
        }
    }

    /**
     * Gives the action standing for an item of a menu the application made itself, choosing it as a click does.
     */
    private static Action wrap(MenuItem item, String path) {
        Action.Builder builder = Action.builder("menu." + path + '.' + item.getText(), item.getText())
                .text(item.textProperty())
                .disabled(item.disableProperty())
                .handler(() -> choose(item));
        if (item.getAccelerator() != null) {
            builder.accelerator(item.getAccelerator());
        }
        if (item instanceof CheckMenuItem check) {
            builder.selected(check.selectedProperty());
        } else if (item instanceof RadioMenuItem radio) {
            builder.selected(radio.selectedProperty());
        }
        return builder.build();
    }

    private static void choose(MenuItem item) {
        if (item instanceof CheckMenuItem check) {
            check.setSelected(!check.isSelected());
        } else if (item instanceof RadioMenuItem radio) {
            radio.setSelected(true);
        }
        item.fire();
    }

    /**
     * Docks the island of a tool window, headed by its title and the buttons of its actions, which follow the tool
     * window as the application changes them, the workbench knowing the actions added and forgetting those removed.
     *
     * @return the subscription, which stops following the actions once unsubscribed
     */
    private Subscription dockIsland(ToolWindow toolWindow) {
        Label title = new Label();
        title.getStyleClass().add("tool-window-title");
        title.textProperty().bind(toolWindow.titleProperty());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox();
        header.getStyleClass().add("tool-window-header");
        Runnable showActions = () -> {
            header.getChildren().setAll(title, spacer);
            toolWindow.getActions().forEach(action -> header.getChildren().add(ActionButtons.create(action)));
            header.getChildren().add(ActionButtons.create(toolWindow.getHideAction()));
        };
        showActions.run();
        ListChangeListener<Action> followActions = change -> {
            while (change.next()) {
                change.getRemoved().forEach(this::unregisterActions);
                change.getAddedSubList().forEach(this::registerActions);
            }
            showActions.run();
        };
        toolWindow.getActions().addListener(followActions);

        BorderPane toolWindowLayout = new BorderPane(toolWindow.getContent());
        toolWindowLayout.setTop(header);
        Island island = new Island(toolWindowLayout);
        island.getStyleClass().add("tool-window");
        islands.dock(toolWindow, island);
        return () -> toolWindow.getActions().removeListener(followActions);
    }
}
