package com.chavaillaz.fx.islands.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("ActionButtons")
class ActionButtonsTest {

    @Test
    @DisplayName("Shows an action as a borderless button of its icon, out of the keyboard focus")
    void showsActionAsButton() {
        SimpleBooleanProperty performed = new SimpleBooleanProperty();
        SimpleBooleanProperty disabled = new SimpleBooleanProperty();
        Action action = Action.builder("test.refresh", "Refresh")
                .icon(Icons.REFRESH)
                .disabled(disabled)
                .handler(() -> performed.set(true))
                .build();
        ButtonBase button = ActionButtons.create(action);

        assertThat(button).isInstanceOf(Button.class);
        assertThat(button.getStyleClass()).containsExactly("action-button");
        assertThat(button.getGraphic()).isInstanceOf(IconView.class);
        assertThat(button.getTooltip().getText()).isEqualTo("Refresh");
        assertThat(button.getAccessibleText()).isEqualTo("Refresh");
        assertThat(button.isFocusTraversable()).isFalse();

        button.fire();
        assertThat(performed.get()).isTrue();

        disabled.set(true);
        assertThat(button.isDisable()).isTrue();
    }

    @Test
    @DisplayName("Shows the text of an action without icon")
    void showsTextWithoutIcon() {
        ButtonBase button = ActionButtons.create(Action.builder("test.run", "Run").handler(() -> { }).build());

        assertThat(button.getText()).isEqualTo("Run");
        assertThat(button.getGraphic()).isNull();
    }

    @Test
    @DisplayName("Shows a toggle action as a toggle button reflecting its state")
    void showsToggleActionAsToggleButton() {
        SimpleBooleanProperty shown = new SimpleBooleanProperty(false);
        Action action = Action.builder("test.toggle", "Orders")
                .icon(Icons.RECORDS)
                .selected(shown)
                .handler(() -> shown.set(!shown.get()))
                .build();
        ButtonBase button = ActionButtons.create(action, 20);

        assertThat(button).isInstanceOfSatisfying(ToggleButton.class, toggleButton -> {
            assertThat(toggleButton.isSelected()).isFalse();
            toggleButton.fire();
            assertThat(shown.get()).isTrue();
            assertThat(toggleButton.isSelected()).isTrue();
            shown.set(false);
            assertThat(toggleButton.isSelected()).isFalse();
        });
        assertThat(((IconView) button.getGraphic()).getPrefWidth()).isEqualTo(20);
    }

    @Test
    @DisplayName("Opens a menu of actions below a menu button")
    void opensMenuBelowButton() {
        Action action = Action.builder("test.run", "Run").handler(() -> { }).build();
        Button button = ActionButtons.createMenuButton("Settings", Icons.SETTINGS, List.of(action), 20);
        Stage stage = new Stage();
        stage.setScene(new Scene(new StackPane(button), 200, 100));
        stage.show();
        try {
            button.fire();

            assertThat(Window.getWindows()).filteredOn(ContextMenu.class::isInstance)
                    .singleElement()
                    .satisfies(menu -> assertThat(((ContextMenu) menu).getItems())
                            .singleElement()
                            .satisfies(item -> assertThat(item.getText()).isEqualTo("Run")));
            assertThat(button.getTooltip().getText()).isEqualTo("Settings");
        } finally {
            stage.hide();
        }
    }
}
