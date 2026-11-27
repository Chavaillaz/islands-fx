package com.chavaillaz.fx.islands.control;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import com.chavaillaz.fx.islands.action.Action;

/**
 * What an area shows while it has nothing to show: a title, a line telling what would fill the area, and links to the
 * actions helping to, each beside its shortcut.
 * <p>
 * It carries the style class {@code empty-state}, its title {@code empty-state-title}, its text
 * {@code empty-state-text}, its links {@code empty-state-action} and their shortcuts {@code empty-state-shortcut}.
 * A part left empty takes no room.
 */
public final class EmptyState extends Control {

    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final ObservableList<Action> actions = FXCollections.observableArrayList();

    /**
     * Creates an empty state showing nothing yet.
     */
    public EmptyState() {
        getStyleClass().add("empty-state");
        // Takes no focus, as a label: given as a style, so that a stylesheet can still change it
        WritableValue<Boolean> focusTraversable = focusTraversableProperty();
        ((StyleableProperty<Boolean>) focusTraversable).applyStyle(null, Boolean.FALSE);
    }

    /**
     * Gives the title, saying in a few words why the area is empty.
     *
     * @return the property of the title, empty for none
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * Gives the title.
     *
     * @return the title, empty for none
     */
    public String getTitle() {
        return title.get();
    }

    /**
     * Changes the title.
     *
     * @param title the title, empty for none
     */
    public void setTitle(String title) {
        this.title.set(title);
    }

    /**
     * Gives the text under the title, telling what would fill the area.
     *
     * @return the property of the text, empty for none
     */
    public StringProperty textProperty() {
        return text;
    }

    /**
     * Gives the text under the title.
     *
     * @return the text, empty for none
     */
    public String getText() {
        return text.get();
    }

    /**
     * Changes the text under the title.
     *
     * @param text the text, empty for none
     */
    public void setText(String text) {
        this.text.set(text);
    }

    /**
     * Gives the actions helping to fill the area, shown as links beside their shortcut.
     *
     * @return the modifiable list of the actions, in the order shown
     */
    public ObservableList<Action> getActions() {
        return actions;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmptyStateSkin(this);
    }

    /**
     * Takes no focus: {@inheritDoc}
     */
    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }
}
