package com.chavaillaz.fx.islands.theme;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.application.ColorScheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Icons of the dialogs")
class DialogIconsTest {

    private final ThemeManager themes = new ThemeManager(ThemeMode.DARK, new SimpleObjectProperty<>(ColorScheme.LIGHT));

    @ParameterizedTest(name = "{0}")
    @CsvSource({"INFORMATION, INFO", "WARNING, WARNING", "ERROR, ERROR", "CONFIRMATION, HELP"})
    @DisplayName("Shows the icon of the library standing for the type of an alert")
    void showsIconOfType(AlertType type, Icons icon) {
        Alert alert = new Alert(type);
        themes.apply(alert.getDialogPane().getScene());

        assertThat(alert.getGraphic()).isInstanceOfSatisfying(IconView.class, view -> {
            assertThat(view.getIcon()).isEqualTo(icon);
            assertThat(view.getStyleClass()).contains("dialog-icon");
            assertThat(view.getPrefWidth()).isEqualTo(DialogIcons.SIZE);
        });
    }

    @Test
    @DisplayName("Asks for a value with the icon of a question")
    void asksWithQuestion() {
        TextInputDialog dialog = new TextInputDialog();
        themes.apply(dialog.getDialogPane().getScene());

        assertThat(dialog.getGraphic()).isInstanceOfSatisfying(IconView.class,
                view -> assertThat(view.getIcon()).isEqualTo(Icons.HELP));
    }

    @Test
    @DisplayName("Follows the type of an alert when it changes")
    void followsTypeChanged() {
        Alert alert = new Alert(AlertType.WARNING);
        themes.apply(alert.getDialogPane().getScene());
        themes.apply(alert.getDialogPane().getScene());

        alert.setAlertType(AlertType.ERROR);
        assertThat(alert.getGraphic()).isInstanceOfSatisfying(IconView.class,
                view -> assertThat(view.getIcon()).isEqualTo(Icons.ERROR));

        alert.setAlertType(AlertType.NONE);
        assertThat(alert.getGraphic()).isNull();
    }

    @Test
    @DisplayName("Keeps the graphic an application gives an alert")
    void keepsGraphicOfApplication() {
        Alert alert = new Alert(AlertType.WARNING);
        Label graphic = new Label("!");
        alert.setGraphic(graphic);
        themes.apply(alert.getDialogPane().getScene());

        assertThat(alert.getGraphic()).isSameAs(graphic);
    }

    @Test
    @DisplayName("Leaves a dialog other than an alert without icon")
    void leavesOtherDialogs() {
        Dialog<Void> dialog = new Dialog<>();
        DialogPane pane = dialog.getDialogPane();
        themes.apply(pane.getScene());

        assertThat(pane.getGraphic()).isNull();
    }
}
