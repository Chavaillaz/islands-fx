package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionMenus;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Workbench")
class WorkbenchTest {

    private final Workbench workbench = new Workbench(false);
    private final BorderPane root = (BorderPane) ((StackPane) workbench.getNode()).getChildren().getFirst();

    @Test
    @DisplayName("Lays out the toolbar, the stripes, the islands and the status bar")
    void laysOutItsParts() {
        assertThat(workbench.getNode().getStyleClass()).contains("workbench");
        assertThat(root.getTop()).isSameAs(workbench.getMainToolbar().getNode());
        assertThat(root.getBottom()).isSameAs(workbench.getStatusBar().getNode());
        assertThat(root.getCenter()).isInstanceOf(IslandsPane.class);
        assertThat(root.getLeft().isVisible()).isFalse();
        assertThat(root.getRight().isVisible()).isFalse();
    }

    @Test
    @DisplayName("Shows the content in the island in the middle")
    void showsContentInMiddle() {
        StackPane content = new StackPane();
        workbench.setContent(content);

        assertThat(content.getParent()).isInstanceOfSatisfying(Island.class,
                island -> assertThat(island.getStyleClass()).contains("island", "content-island"));
    }

    @Test
    @DisplayName("Docks a tool window in an island headed by its title and actions, its button on the stripe")
    void docksToolWindow() {
        Action refresh = Action.builder("test.refresh", "Refresh").icon(Icons.REFRESH).handler(() -> { }).build();
        StackPane content = new StackPane();
        ToolWindow toolWindow = ToolWindow.builder("records", "Orders", Icons.RECORDS, content)
                .actions(refresh)
                .build();
        workbench.addToolWindow(toolWindow);

        assertThat(workbench.getToolWindows()).containsExactly(toolWindow);
        assertThat(root.getLeft().isVisible()).isTrue();
        assertThat(root.getCenter().getPseudoClassStates()).contains(PseudoClass.getPseudoClass("left-stripe"));
        BorderPane layout = (BorderPane) content.getParent();
        HBox header = (HBox) layout.getTop();
        assertThat(header.getStyleClass()).contains("tool-window-header");
        assertThat(((Label) header.getChildren().getFirst()).getText()).isEqualTo("Orders");
        assertThat(header.getChildren()).filteredOn(ButtonBase.class::isInstance).hasSize(2);

        ((ButtonBase) header.getChildren().getLast()).fire();
        assertThat(toolWindow.isVisible()).isFalse();
        assertThat(layout.getParent().isManaged()).isFalse();
    }

    @Test
    @DisplayName("Docks a tool window on the right, its button on the stripe of the right")
    void docksToolWindowOnRight() {
        workbench.addToolWindow(ToolWindow.builder("structure", "Structure", Icons.OBJECT, new StackPane())
                .anchor(ToolWindowAnchor.RIGHT)
                .build());

        assertThat(root.getLeft().isVisible()).isFalse();
        assertThat(root.getRight().isVisible()).isTrue();
        assertThat(((ToolWindowStripe) root.getRight()).getButtons()).singleElement()
                .satisfies(button -> assertThat(button.getStyleClass()).contains("stripe-button"));
    }

    @Test
    @DisplayName("Refuses a second tool window of the same identifier")
    void refusesDuplicateToolWindow() {
        workbench.addToolWindow(ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane()).build());
        ToolWindow duplicate = ToolWindow.builder("records", "Again", Icons.RECORDS, new StackPane()).build();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> workbench.addToolWindow(duplicate))
                .withMessageContaining("records");
    }

    @Test
    @DisplayName("Shows the path to what the window shows as breadcrumbs")
    void showsPathAsBreadcrumbs() {
        StatusBar statusBar = workbench.getStatusBar();
        statusBar.getPath().setAll("Orders", "SO-10042");
        Label count = new Label("150 records");
        statusBar.getWidgets().add(count);

        HBox breadcrumbs = (HBox) ((HBox) statusBar.getNode()).getChildren().getFirst();
        assertThat(breadcrumbs.getChildren()).hasSize(3);
        assertThat(breadcrumbs.getChildren()).filteredOn(Label.class::isInstance)
                .extracting(node -> ((Label) node).getText())
                .containsExactly("Orders", "SO-10042");
        assertThat(count.getParent().getStyleClass()).contains("status-widgets");

        statusBar.getPath().clear();
        assertThat(breadcrumbs.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("Holds the items of the main toolbar on its left and right")
    void holdsToolbarItems() {
        MainToolbar toolbar = workbench.getMainToolbar();
        Node left = new Label("Left");
        Node right = new Label("Right");
        toolbar.getLeftItems().add(left);
        toolbar.getRightItems().add(right);

        assertThat(toolbar.getNode().getStyleClass()).contains("main-toolbar");
        assertThat(toolbar.getNode().getMinHeight()).isEqualTo(MainToolbar.HEIGHT);
        assertThat(left.getParent().getStyleClass()).contains("toolbar-left");
        assertThat(right.getParent().getStyleClass()).contains("toolbar-right");
    }

    @Test
    @DisplayName("Clips the content of an island to its rounded corners")
    void clipsIslandContent() {
        StackPane content = new StackPane();
        Island island = new Island(content);
        island.resize(300, 200);

        assertThat(island.getContent()).isSameAs(content);
        assertThat(island.getClip().getLayoutBounds().getWidth()).isEqualTo(300);
        island.setContent(null);
        assertThat(island.getContent()).isNull();
    }

    @Test
    @DisplayName("Opens its main menu from the first button of the main toolbar, its shortcuts working while closed")
    void opensMainMenuFromToolbar() {
        SimpleBooleanProperty refreshed = new SimpleBooleanProperty();
        KeyCombination accelerator = KeyCombination.keyCombination("Shortcut+R");
        MenuItem refresh = new MenuItem("Refresh");
        refresh.setAccelerator(accelerator);
        refresh.setOnAction(event -> refreshed.set(true));
        Menu file = new Menu("File");
        file.getItems().add(new Menu("Recent", null, refresh));
        workbench.getMainToolbar().getLeftItems().add(new TitleWidget("Record Explorer"));
        workbench.setMainMenu(List.of(file));
        Stage stage = new Stage();
        Scene scene = workbench.createScene(stage);

        assertThat(workbench.hasSystemMenuBar()).isFalse();
        assertThat(stage.getStyle()).isEqualTo(StageStyle.EXTENDED);
        assertThat(stage.getScene()).isSameAs(scene);
        assertThat(workbench.getMainToolbar().getLeftItems().getFirst())
                .isInstanceOfSatisfying(ButtonBase.class,
                        button -> assertThat(button.getAccessibleText()).isEqualTo("Main Menu"));
        assertThat(scene.getAccelerators()).containsKey(accelerator);

        scene.getAccelerators().get(accelerator).run();
        assertThat(refreshed.get()).isTrue();
    }

    @Test
    @DisplayName("Carries its main menu to the menu bar of the system, where it has one")
    void carriesMainMenuToSystemMenuBar() {
        Workbench systemWorkbench = new Workbench(true);
        systemWorkbench.setMainMenu(List.of(new Menu("File")));

        assertThat(((StackPane) systemWorkbench.getNode()).getChildren())
                .filteredOn(MenuBar.class::isInstance)
                .singleElement()
                .satisfies(menuBar -> {
                    assertThat(((MenuBar) menuBar).isUseSystemMenuBar()).isTrue();
                    assertThat(menuBar.isManaged()).isFalse();
                });
        assertThat(systemWorkbench.getMainToolbar().getLeftItems()).isEmpty();
        assertThat(systemWorkbench.hasSystemMenuBar()).isTrue();
    }

    @Test
    @DisplayName("Titles the window with the initials of its title in a badge")
    void titlesWindowWithInitials() {
        assertThat(TitleWidget.initials("Record Explorer")).isEqualTo("RE");
        assertThat(TitleWidget.initials("QAFFE KT Viewer")).isEqualTo("QK");
        assertThat(TitleWidget.initials("explorer")).isEqualTo("EX");
        assertThat(TitleWidget.initials(" x ")).isEqualTo("X");
        assertThat(new TitleWidget("Record Explorer").getStyleClass()).contains("title-widget");

        TitleWidget widget = new TitleWidget();
        widget.setTitle("Order Desk");
        assertThat(widget.getChildren()).extracting(node -> ((Label) node).getText()).containsExactly("OD", "Order Desk");
        assertThat(widget.getTitle()).isEqualTo("Order Desk");
    }

    @Test
    @DisplayName("Performs the shortcuts of its tool windows and of the actions registered, out of any menu")
    void performsShortcutsOutOfMenus() {
        SimpleBooleanProperty refreshed = new SimpleBooleanProperty();
        KeyCombination showRecords = KeyCombination.keyCombination("Shortcut+1");
        KeyCombination refresh = KeyCombination.keyCombination("Shortcut+R");
        ToolWindow records = ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane())
                .accelerator(showRecords)
                .build();
        workbench.addToolWindow(records);
        Scene scene = workbench.createScene(new Stage());
        workbench.registerActions(
                Action.builder("test.refresh", "Refresh").accelerator(refresh).handler(() -> refreshed.set(true)).build(),
                Action.builder("test.again", "Again").accelerator(refresh).handler(() -> { }).build());

        scene.getAccelerators().get(showRecords).run();
        scene.getAccelerators().get(refresh).run();
        assertThat(records.isVisible()).isFalse();
        assertThat(refreshed.get()).isTrue();
    }

    @Test
    @DisplayName("Leaves the shortcuts of its main menu to the menu bar of the system, where it has one")
    void leavesMenuShortcutsToSystem() {
        Workbench systemWorkbench = new Workbench(true);
        KeyCombination showRecords = KeyCombination.keyCombination("Shortcut+1");
        ToolWindow records = ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane())
                .accelerator(showRecords)
                .build();
        systemWorkbench.addToolWindow(records);
        Scene scene = systemWorkbench.createScene(new Stage());
        assertThat(scene.getAccelerators()).containsKey(showRecords);

        systemWorkbench.setMainMenu(List.of(new Menu("View", null, ActionMenus.createItem(records.getToggleAction()))));
        systemWorkbench.registerActions(records.getToggleAction());
        assertThat(scene.getAccelerators()).doesNotContainKey(showRecords)
                .containsKey(systemWorkbench.getSearchAction().getAccelerator());
    }

    @Test
    @DisplayName("Docks a tool window under the content, its button at the bottom of the left stripe")
    void docksToolWindowAtBottom() {
        ToolWindow records = ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane()).build();
        ToolWindow log = ToolWindow.builder("log", "Log", Icons.LIST, new StackPane())
                .anchor(ToolWindowAnchor.BOTTOM)
                .build();
        workbench.addToolWindow(records);
        workbench.addToolWindow(log);

        ToolWindowStripe stripe = (ToolWindowStripe) root.getLeft();
        assertThat(stripe.getButtons()).hasSize(2);
        assertThat(((VBox) stripe.getChildren().getLast()).getChildren()).containsExactly(stripe.getButtons().getLast());
        assertThat(log.getHeight()).isEqualTo(ToolWindow.DEFAULT_HEIGHT);
    }

    @Test
    @DisplayName("Shows one tool window at a time under the content")
    void showsOneToolWindowAtBottom() {
        ToolWindow log = ToolWindow.builder("log", "Log", Icons.LIST, new StackPane())
                .anchor(ToolWindowAnchor.BOTTOM)
                .build();
        ToolWindow problems = ToolWindow.builder("problems", "Problems", Icons.WARNING, new StackPane())
                .anchor(ToolWindowAnchor.BOTTOM)
                .height(180)
                .build();
        workbench.addToolWindow(log);
        workbench.addToolWindow(problems);
        assertThat(log.isVisible()).isTrue();
        assertThat(problems.isVisible()).isFalse();

        problems.getToggleAction().perform();
        assertThat(problems.isVisible()).isTrue();
        assertThat(log.isVisible()).isFalse();
    }
}
