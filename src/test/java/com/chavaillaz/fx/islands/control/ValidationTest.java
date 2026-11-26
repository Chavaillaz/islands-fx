package com.chavaillaz.fx.islands.control;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Validation")
class ValidationTest {

    @Test
    @DisplayName("Marks an input filled in wrong, its tooltip telling why")
    void marksError() {
        TextField field = new TextField();
        Validation.setError(field, "The name is required");

        assertThat(Validation.getMark(field)).isEqualTo(Validation.ERROR);
        assertThat(field.getTooltip().getText()).isEqualTo("The name is required");
        assertThat(field.getTooltip().getStyleClass()).contains("validation-tooltip", "error");
    }

    @Test
    @DisplayName("Replaces the mark of an input, and gives it its former tooltip back once cleared")
    void replacesThenClearsMark() {
        TextField field = new TextField();
        Tooltip former = new Tooltip("Your name");
        field.setTooltip(former);
        Validation.setError(field, "The name is required");
        Validation.setWarning(field, "The name is long");

        assertThat(Validation.getMark(field)).isEqualTo(Validation.WARNING);
        assertThat(field.getPseudoClassStates()).doesNotContain(Validation.ERROR);
        assertThat(field.getTooltip().getText()).isEqualTo("The name is long");
        assertThat(field.getTooltip().getStyleClass()).contains("warning").doesNotContain("error");

        Validation.clear(field);
        assertThat(Validation.getMark(field)).isNull();
        assertThat(field.getTooltip()).isSameAs(former);

        Validation.clear(field);
        assertThat(field.getTooltip()).isSameAs(former);
    }
}
