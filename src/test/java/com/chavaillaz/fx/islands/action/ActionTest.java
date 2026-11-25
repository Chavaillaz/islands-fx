package com.chavaillaz.fx.islands.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.KeyCombination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Action")
class ActionTest {

    private final AtomicInteger performed = new AtomicInteger();

    @Test
    @DisplayName("Runs its handler when performed")
    void runsHandler() {
        Action action = Action.builder("test.run", "Run").handler(performed::incrementAndGet).build();
        action.perform();

        assertThat(performed).hasValue(1);
        assertThat(action.getId()).isEqualTo("test.run");
        assertThat(action.getText()).isEqualTo("Run");
        assertThat(action.getIcon()).isNull();
        assertThat(action.getAccelerator()).isNull();
        assertThat(action.getDescription()).isNull();
        assertThat(action).hasToString("Action[test.run]");
    }

    @Test
    @DisplayName("Does nothing when performed while disabled")
    void ignoresPerformWhileDisabled() {
        SimpleBooleanProperty disabled = new SimpleBooleanProperty(true);
        Action action = Action.builder("test.run", "Run")
                .disabled(disabled)
                .handler(performed::incrementAndGet)
                .build();
        action.perform();
        assertThat(performed).hasValue(0);
        assertThat(action.isDisabled()).isTrue();

        disabled.set(false);
        action.perform();
        assertThat(performed).hasValue(1);
        assertThat(action.disabledProperty().get()).isFalse();
    }

    @Test
    @DisplayName("Reflects the state a toggle action toggles")
    void reflectsToggledState() {
        SimpleBooleanProperty shown = new SimpleBooleanProperty(true);
        Action action = Action.builder("test.toggle", "Show")
                .selected(shown)
                .handler(() -> shown.set(!shown.get()))
                .build();
        assertThat(action.isToggle()).isTrue();
        assertThat(action.isSelected()).isTrue();

        action.perform();
        assertThat(action.isSelected()).isFalse();
        assertThat(action.selectedProperty().get()).isFalse();
    }

    @Test
    @DisplayName("Is never selected when it toggles nothing")
    void isNeverSelectedWithoutState() {
        Action action = Action.builder("test.run", "Run").handler(performed::incrementAndGet).build();

        assertThat(action.isToggle()).isFalse();
        assertThat(action.isSelected()).isFalse();
    }

    @Test
    @DisplayName("Presents its text, shortcut and description in its tooltip")
    void presentsItselfInTooltip() {
        KeyCombination accelerator = KeyCombination.keyCombination("Shortcut+R");
        Action action = Action.builder("test.refresh", "Refresh")
                .description("Loads the records again")
                .icon(Icons.REFRESH)
                .accelerator(accelerator)
                .handler(performed::incrementAndGet)
                .build();

        assertThat(action.tooltip())
                .isEqualTo("Refresh  " + accelerator.getDisplayText() + "\nLoads the records again");
        assertThat(action.getIcon()).isEqualTo(Icons.REFRESH);
        assertThat(action.getAccelerator()).isEqualTo(accelerator);
    }

    @Test
    @DisplayName("Needs a handler")
    void needsHandler() {
        Action.Builder builder = Action.builder("test.nothing", "Nothing");

        assertThatNullPointerException()
                .isThrownBy(builder::build)
                .withMessageContaining("test.nothing");
    }
}
