package com.chavaillaz.fx.islands.control;

import javafx.scene.control.Label;

/**
 * A short text in a rounded label tinted after its {@link Tone}, such as the status of a record.
 * <p>
 * The badge carries the style class {@code badge}, and the pseudo-class of its tone, such as {@code :success}.
 */
public final class Badge extends Label {

    private Tone tone = Tone.NEUTRAL;

    /**
     * Creates a badge without text, of a neutral tone.
     */
    public Badge() {
        getStyleClass().add("badge");
        setMinWidth(USE_PREF_SIZE);
        pseudoClassStateChanged(tone.pseudoClass(), true);
    }

    /**
     * Shows another text in another tone, a cell showing a badge reusing it for each value.
     *
     * @param text the text
     * @param tone the tone
     */
    public void update(String text, Tone tone) {
        setText(text);
        setTone(tone);
    }

    /**
     * Gives the tone of the badge.
     *
     * @return the tone
     */
    public Tone getTone() {
        return tone;
    }

    /**
     * Changes the tone of the badge.
     *
     * @param tone the tone
     */
    public void setTone(Tone tone) {
        pseudoClassStateChanged(this.tone.pseudoClass(), false);
        this.tone = tone;
        pseudoClassStateChanged(tone.pseudoClass(), true);
    }
}
