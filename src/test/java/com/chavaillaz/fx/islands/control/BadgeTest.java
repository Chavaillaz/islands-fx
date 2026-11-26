package com.chavaillaz.fx.islands.control;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Badge")
class BadgeTest {

    @Test
    @DisplayName("Takes the pseudo-class of its tone only")
    void takesPseudoClassOfItsTone() {
        Badge badge = new Badge();
        assertThat(badge.getStyleClass()).contains("badge");
        assertThat(badge.getTone()).isEqualTo(Tone.NEUTRAL);
        assertThat(badge.getPseudoClassStates()).containsExactly(Tone.NEUTRAL.pseudoClass());

        badge.update("Delivered", Tone.SUCCESS);

        assertThat(badge.getText()).isEqualTo("Delivered");
        assertThat(badge.getTone()).isEqualTo(Tone.SUCCESS);
        assertThat(badge.getPseudoClassStates()).containsExactly(Tone.SUCCESS.pseudoClass());
        assertThat(Tone.SUCCESS.pseudoClass().getPseudoClassName()).isEqualTo("success");
    }

    @Test
    @DisplayName("Follows the tone it is bound to")
    void followsBoundTone() {
        SimpleObjectProperty<Tone> status = new SimpleObjectProperty<>(Tone.WARNING);
        Badge badge = new Badge();
        badge.toneProperty().bind(status);
        assertThat(badge.getPseudoClassStates()).containsExactly(Tone.WARNING.pseudoClass());

        status.set(Tone.DANGER);
        assertThat(badge.getPseudoClassStates()).containsExactly(Tone.DANGER.pseudoClass());
    }
}
