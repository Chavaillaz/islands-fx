package com.chavaillaz.fx.islands.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.internal.Texts;

/**
 * The layout of the main window: the main toolbar along the top, the status bar along the bottom, the stripes of tool
 * window buttons along the sides, and the islands in between: the content in the middle, the tool windows docked
 * beside it.
 * <p>
 * The workbench is the scene of its window ({@link #createScene(Stage)}), its main toolbar standing in the title bar.
 * Its main menu stands in the menu bar of macOS, and elsewhere in a menu opened by the first button of the main
 * toolbar.
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

    /**
     * Creates a workbench without content nor tool window yet, its main menu standing in the menu bar of the system
     * on macOS only.
     */
    public Workbench() {
        this(System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("mac"));
    }

    /**
     * Creates a workbench without content nor tool window yet.
     *
     * @param systemMenuBar whether the main menu stands in the menu bar of the system, rather than the main toolbar
     */
    Workbench(boolean systemMenuBar) {
        this.systemMenuBar = systemMenuBar;
        root.getStyleClass().add("workbench");
        root.getChildren().add(layout);
        content.getStyleClass().add("content-island");
        layout.setTop(mainToolbar.getNode());
        layout.setLeft(leftStripe);
        layout.setCenter(islands);
        layout.setRight(rightStripe);
        layout.setBottom(statusBar.getNode());
        leftStripe.visibleProperty().subscribe(shown -> islands.pseudoClassStateChanged(LEFT_STRIPE, shown));
        rightStripe.visibleProperty().subscribe(shown -> islands.pseudoClassStateChanged(RIGHT_STRIPE, shown));
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
     * side.
     *
     * @param toolWindow the tool window
     * @throws IllegalArgumentException when a tool window of the same identifier is already docked
     */
    public void addToolWindow(ToolWindow toolWindow) {
        if (toolWindows.stream().anyMatch(each -> each.getId().equals(toolWindow.getId()))) {
            throw new IllegalArgumentException("A tool window named " + toolWindow.getId() + " is already docked");
        }
        toolWindows.add(toolWindow);
        islands.dock(toolWindow, createIsland(toolWindow));
        (toolWindow.getAnchor() == ToolWindowAnchor.LEFT ? leftStripe : rightStripe).add(toolWindow);
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
     * Tells whether the main menu stands in the menu bar of the system, whose menu of the application then holds the
     * item quitting it, rather than the main toolbar.
     *
     * @return {@code true} on macOS
     */
    public boolean hasSystemMenuBar() {
        return systemMenuBar;
    }

    /**
     * Gives the window its main menu, once: in the menu bar of the system on macOS, elsewhere in a menu opened by a
     * button heading the main toolbar, whose shortcuts are then given to the scene, so that they work while the menu
     * is closed.
     *
     * @param menus the menus, in the order of the menu bar
     */
    public void setMainMenu(List<Menu> menus) {
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
            root.sceneProperty().subscribe(scene -> {
                if (scene != null) {
                    registerAccelerators(scene, menus);
                }
            });
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

    private static void registerAccelerators(Scene scene, List<? extends MenuItem> items) {
        for (MenuItem item : items) {
            if (item instanceof Menu menu) {
                registerAccelerators(scene, menu.getItems());
            } else if (item.getAccelerator() instanceof KeyCombination accelerator) {
                scene.getAccelerators().put(accelerator, item::fire);
            }
        }
    }

    private static Island createIsland(ToolWindow toolWindow) {
        Label title = new Label(toolWindow.getTitle());
        title.getStyleClass().add("tool-window-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer);
        header.getStyleClass().add("tool-window-header");
        toolWindow.getActions().forEach(action -> header.getChildren().add(ActionButtons.create(action)));
        header.getChildren().add(ActionButtons.create(toolWindow.getHideAction()));
        BorderPane toolWindowLayout = new BorderPane(toolWindow.getContent());
        toolWindowLayout.setTop(header);
        Island island = new Island(toolWindowLayout);
        island.getStyleClass().add("tool-window");
        return island;
    }
}
