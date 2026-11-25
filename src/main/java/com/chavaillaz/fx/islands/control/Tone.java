package com.chavaillaz.fx.islands.control;

import java.util.Locale;

import javafx.css.PseudoClass;

/**
 * The tone of a {@link Badge}, telling at a glance how things stand, which the stylesheets color.
 */
public enum Tone {

    /** Nothing to notice. */
    NEUTRAL,

    /** Something going on as expected. */
    INFO,

    /** Something done, or going well. */
    SUCCESS,

    /** Something waiting for attention. */
    WARNING,

    /** Something failed, or stopped. */
    DANGER;

    private final PseudoClass pseudoClass = PseudoClass.getPseudoClass(name().toLowerCase(Locale.ROOT));

    /**
     * Gives the pseudo-class a badge of this tone takes, such as {@code :success}.
     *
     * @return the pseudo-class, named after the tone in lowercase
     */
    public PseudoClass pseudoClass() {
        return pseudoClass;
    }
}
