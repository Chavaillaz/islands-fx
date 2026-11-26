package com.chavaillaz.fx.islands.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

/**
 * A short text in a rounded label tinted after its {@link Tone}, such as the status of a record.
 * <p>
 * The badge carries the style class {@code badge}, and the pseudo-class of its tone, such as {@code :success}. An FXML
 * file declares it as {@code <Badge text="Paid" tone="SUCCESS"/>}.
 */
public final class Badge extends Label {

    private final ObjectProperty<Tone> tone = new SimpleObjectProperty<>(this, "tone", Tone.NEUTRAL);

    /**
     * Creates a badge without text, of a neutral tone.
     */
    public Badge() {
        getStyleClass().add("badge");
        setMinWidth(USE_PREF_SIZE);
        tone.subscribe((previous, current) -> pseudoClassStateChanged(previous.pseudoClass(), false));
        tone.subscribe(current -> pseudoClassStateChanged(current.pseudoClass(), true));
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
     * @return the property of the tone, neutral unless told otherwise
     */
    public ObjectProperty<Tone> toneProperty() {
        return tone;
    }

    /**
     * Gives the tone of the badge.
     *
     * @return the tone
     */
    public Tone getTone() {
        return tone.get();
    }

    /**
     * Changes the tone of the badge.
     *
     * @param tone the tone
     */
    public void setTone(Tone tone) {
        this.tone.set(tone);
    }
}
