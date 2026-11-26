package com.chavaillaz.fx.islands.control;

import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;

/**
 * Marks the inputs the user filled in wrong, or doubtfully: the outline of an input takes the color of the problem,
 * stronger while it has the focus, and its tooltip tells the problem.
 * <p>
 * An input marked carries the pseudo-class {@code :error} or {@code :warning}, and its tooltip the style classes
 * {@code validation-tooltip} and {@code error} or {@code warning}. The tooltip the input had before comes back once the
 * mark is cleared. The stylesheets outline the text inputs, the combo boxes, the choice boxes and the spinners.
 */
public final class Validation {

    /** The pseudo-class of an input filled in wrong. */
    public static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    /** The pseudo-class of an input filled in doubtfully. */
    public static final PseudoClass WARNING = PseudoClass.getPseudoClass("warning");

    /** The key keeping, in the properties of an input marked, the tooltip it had before. */
    private static final String TOOLTIP_KEY = Validation.class.getName() + ".tooltip";

    /** How long the mouse rests on an input before its problem shows, shorter than for other tooltips. */
    private static final Duration SHOW_DELAY = Duration.millis(200);

    private Validation() {
    }

    /**
     * Marks an input filled in wrong, replacing the mark it had.
     *
     * @param control the input
     * @param message what is wrong, which its tooltip tells
     */
    public static void setError(Control control, String message) {
        mark(control, ERROR, message);
    }

    /**
     * Marks an input filled in doubtfully, replacing the mark it had.
     *
     * @param control the input
     * @param message what is doubtful, which its tooltip tells
     */
    public static void setWarning(Control control, String message) {
        mark(control, WARNING, message);
    }

    /**
     * Clears the mark of an input, which gets its former tooltip back. An input not marked is left alone.
     *
     * @param control the input
     */
    public static void clear(Control control) {
        if (control.getProperties().containsKey(TOOLTIP_KEY)) {
            control.pseudoClassStateChanged(ERROR, false);
            control.pseudoClassStateChanged(WARNING, false);
            control.setTooltip((Tooltip) control.getProperties().remove(TOOLTIP_KEY));
        }
    }

    /**
     * Tells what an input is marked with.
     *
     * @param control the input
     * @return {@link #ERROR}, {@link #WARNING}, or {@code null} for an input not marked
     */
    public static @Nullable PseudoClass getMark(Control control) {
        if (control.getPseudoClassStates().contains(ERROR)) {
            return ERROR;
        }
        return control.getPseudoClassStates().contains(WARNING) ? WARNING : null;
    }

    private static void mark(Control control, PseudoClass state, String message) {
        if (!control.getProperties().containsKey(TOOLTIP_KEY)) {
            control.getProperties().put(TOOLTIP_KEY, control.getTooltip());
        }
        control.pseudoClassStateChanged(ERROR, state == ERROR);
        control.pseudoClassStateChanged(WARNING, state == WARNING);
        Tooltip tooltip = new Tooltip(message);
        tooltip.getStyleClass().addAll("validation-tooltip", state.getPseudoClassName());
        tooltip.setShowDelay(SHOW_DELAY);
        control.setTooltip(tooltip);
    }
}
