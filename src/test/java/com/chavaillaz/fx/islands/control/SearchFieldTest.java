package com.chavaillaz.fx.islands.control;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("SearchField")
class SearchFieldTest {

    private final SearchField field = new SearchField();
    private final SearchFieldSkin skin;

    SearchFieldTest() {
        VBox root = new VBox(field);
        new Scene(root, 300, 100);
        root.applyCss();
        root.layout();
        skin = (SearchFieldSkin) field.getSkin();
    }

    @Test
    @DisplayName("Shows a magnifier before its text, and a button clearing it once filled")
    void showsMagnifierAndClearButton() {
        assertThat(field.getStyleClass()).contains("text-field", "search-field");
        assertThat(field.getChildrenUnmodifiable()).filteredOn(IconView.class::isInstance).singleElement()
                .satisfies(icon -> {
                    assertThat(((IconView) icon).getIcon()).isEqualTo(Icons.SEARCH);
                    assertThat(icon.getLayoutX()).isLessThan(skin.getClearButton().getLayoutX());
                });
        StackPane clear = skin.getClearButton();
        assertThat(clear.isVisible()).isFalse();

        field.setText("SO-1");
        assertThat(clear.isVisible()).isTrue();
        clear.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
        assertThat(field.getText()).isEmpty();
        assertThat(field.prefWidth(-1)).isGreaterThan(SearchFieldSkin.CLEAR_SIZE + IconView.GRID);
    }

    @Test
    @DisplayName("Keeps its size from one layout to the next, however wide it is laid out")
    void keepsItsSize() {
        double minWidth = field.minWidth(-1);
        double prefWidth = field.prefWidth(-1);
        for (int pass = 0; pass < 3; pass++) {
            field.resize(600, 30);
            field.layout();
        }

        assertThat(field.minWidth(-1)).isEqualTo(minWidth).isLessThan(prefWidth);
        assertThat(field.prefWidth(-1)).isEqualTo(prefWidth);
        assertThat(skin.getClearButton().getLayoutX()).isGreaterThan(500);
    }

    @Test
    @DisplayName("Clears its text on the escape key, which goes on once the field is empty")
    void clearsOnEscape() {
        field.setText("SO-1");
        KeyEvent first = escape();
        field.fireEvent(first);
        assertThat(field.getText()).isEmpty();

        boolean[] reached = new boolean[1];
        field.getParent().addEventHandler(KeyEvent.KEY_PRESSED, event -> reached[0] = true);
        field.fireEvent(escape());
        assertThat(reached[0]).isTrue();
    }

    private static KeyEvent escape() {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false);
    }
}
