package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionGroup;
import com.chavaillaz.fx.islands.action.ActionMenus;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Search of the actions")
class ActionSearchTest {

    private final Stage stage = new Stage();

    @AfterEach
    void hideStage() {
        stage.hide();
    }

    @Test
    @DisplayName("Finds the actions holding every word typed, those starting with it first, whatever case and accents")
    void findsBestMatchesFirst() {
        ActionSearch.Entry formatting = entry("Reformat Code", "Code", null);
        ActionSearch.Entry refresh = entry("Refresh", "File", null);
        ActionSearch.Entry areas = entry("Show Areas", "", null);
        ActionSearch.Entry expand = entry("Expand All", "", "Expands every node of the tree");
        ActionSearch.Entry theme = entry("Thème", "View", null);
        List<ActionSearch.Entry> entries = List.of(areas, formatting, refresh, expand, theme);

        assertThat(ActionSearch.filter(entries, "re")).containsExactly(formatting, refresh, areas, expand);
        assertThat(ActionSearch.filter(entries, "NODE")).containsExactly(expand);
        assertThat(ActionSearch.filter(entries, "view theme")).containsExactly(theme);
        assertThat(ActionSearch.filter(entries, "  ")).isEqualTo(entries);
        assertThat(ActionSearch.filter(entries, "nothing")).isEmpty();
    }

    @Test
    @DisplayName("Opens over the window from its action, performing the action chosen with the keyboard")
    void performsActionChosen() {
        SimpleBooleanProperty opened = new SimpleBooleanProperty();
        Workbench workbench = new Workbench(false);
        workbench.addToolWindow(ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane()).build());
        workbench.setMainMenu(ActionMenus.createMenus(List.of(ActionGroup.of("File",
                Action.builder("file.open", "Open…").handler(() -> opened.set(true)).build()))));
        show(workbench);
        ActionSearch search = workbench.getSearch();

        workbench.getSearchAction().perform();
        assertThat(search.isOpen()).isTrue();
        assertThat(search.getList().getItems()).extracting(entry -> entry.action().getText())
                .containsExactly("Open…", "Orders");
        assertThat(search.getList().getItems().getFirst().path()).isEqualTo("File");

        TextField field = search.getField();
        field.setText("ord");
        assertThat(search.getList().getItems()).extracting(entry -> entry.action().getText()).containsExactly("Orders");
        press(field, KeyCode.ENTER);
        assertThat(search.isOpen()).isFalse();
        assertThat(workbench.getToolWindows().getFirst().isVisible()).isFalse();

        workbench.getSearchAction().perform();
        press(field, KeyCode.DOWN);
        press(field, KeyCode.UP);
        press(field, KeyCode.ENTER);
        assertThat(opened.get()).isTrue();
    }

    @Test
    @DisplayName("Closes on the escape key, the focus going back where it was")
    void closesOnEscape() {
        Workbench workbench = new Workbench(false);
        TextField editor = new TextField();
        workbench.setContent(editor);
        show(workbench);
        editor.requestFocus();

        workbench.getSearchAction().perform();
        workbench.getSearchAction().perform();
        assertThat(workbench.getSearch().getField().isFocused()).isTrue();
        press(workbench.getSearch().getField(), KeyCode.ESCAPE);

        assertThat(workbench.getSearch().isOpen()).isFalse();
        assertThat(editor.isFocused()).isTrue();
    }

    @Test
    @DisplayName("Finds the items of the menus the application made, choosing them as a click does")
    void findsItemsOfApplication() {
        CheckMenuItem wrap = new CheckMenuItem("Word Wrap");
        MenuItem untitled = new MenuItem();
        Workbench workbench = new Workbench(false);
        workbench.setMainMenu(List.of(new Menu("View", null, new Menu("Editor", null, wrap, untitled))));

        assertThat(workbench.searchEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.path()).isEqualTo("View › Editor");
            assertThat(entry.action().isToggle()).isTrue();
            entry.action().perform();
        });
        assertThat(wrap.isSelected()).isTrue();
    }

    private void show(Workbench workbench) {
        Scene scene = workbench.createScene(stage);
        stage.setWidth(900);
        stage.setHeight(600);
        stage.show();
        scene.getRoot().applyCss();
        scene.getRoot().layout();
    }

    private static void press(TextField field, KeyCode key) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, false, false, false, false));
    }

    private static ActionSearch.Entry entry(String text, String path, String description) {
        Action.Builder builder = Action.builder("test." + text, text).handler(() -> { });
        if (description != null) {
            builder.description(description);
        }
        return new ActionSearch.Entry(builder.build(), path);
    }
}
