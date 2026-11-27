package com.chavaillaz.fx.islands.workbench;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.beans.NamedArg;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.WritableValue;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderDragType;

/**
 * The title of the window in the main toolbar: a badge of the initials of the title, then the title. Dragging it
 * moves the window, as dragging a title bar does.
 * <p>
 * It carries the style class {@code title-widget}, its badge {@code title-badge} and its title {@code title-name}. An
 * FXML file declares it as {@code <TitleWidget title="Record Explorer"/>}.
 */
public final class TitleWidget extends Control {

    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    /**
     * Creates the widget of no title yet.
     */
    public TitleWidget() {
        getStyleClass().add("title-widget");
        // Takes no focus, as a label: given as a style, so that a stylesheet can still change it
        WritableValue<Boolean> focusTraversable = focusTraversableProperty();
        ((StyleableProperty<Boolean>) focusTraversable).applyStyle(null, Boolean.FALSE);
        HeaderBar.setDragType(this, HeaderDragType.DRAGGABLE_SUBTREE);
    }

    /**
     * Creates the widget of a title.
     *
     * @param title the title, such as the name of the application
     */
    public TitleWidget(@NamedArg("title") String title) {
        this();
        setTitle(title);
    }

    /**
     * Gives the title the widget shows, after the badge of its initials.
     *
     * @return the property of the title, empty for none
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * Gives the title the widget shows.
     *
     * @return the title, empty for none
     */
    public String getTitle() {
        return title.get();
    }

    /**
     * Changes the title the widget shows.
     *
     * @param title the title, such as the name of the application
     */
    public void setTitle(String title) {
        this.title.set(title);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TitleWidgetSkin(this);
    }

    /**
     * Takes no focus: {@inheritDoc}
     */
    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    /**
     * Gives the initials of a title, as its badge shows them.
     *
     * @param title the title
     * @return the first letters of its first two words, or the first two letters of its single word, in uppercase
     */
    static String initials(String title) {
        String[] words = title.strip().split("\\s+");
        String initials = words.length > 1
                ? Arrays.stream(words).limit(2).map(word -> word.substring(0, 1)).collect(Collectors.joining())
                : words[0].substring(0, Math.min(2, words[0].length()));
        return initials.toUpperCase(Locale.ROOT);
    }
}
