package com.chavaillaz.fx.islands.workbench;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.WritableValue;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.shape.Rectangle;
import org.jspecify.annotations.Nullable;

/**
 * A panel with rounded corners standing out of the background of the window, holding a tool window or the content of
 * the window: its content fills it, clipped to its corners.
 * <p>
 * It carries the style class {@code island}, whose background the stylesheets round by {@link #RADIUS} as well. An FXML
 * file declares its content as its child, {@code <Island><ListView/></Island>}. Its skin is created along with it,
 * unlike those of the controls of JavaFX: its content stands in it at once, before any styling.
 */
@DefaultProperty("content")
public final class Island extends Control {

    /** The radius of the corners in pixels, which the stylesheets give the background of an island too. */
    public static final double RADIUS = 10;

    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /**
     * Creates an island showing nothing yet.
     */
    public Island() {
        getStyleClass().add("island");
        // Takes no focus, as a label: given as a style, so that a stylesheet can still change it
        WritableValue<Boolean> focusTraversable = focusTraversableProperty();
        ((StyleableProperty<Boolean>) focusTraversable).applyStyle(null, Boolean.FALSE);
        Rectangle clip = new Rectangle();
        clip.setArcWidth(2 * RADIUS);
        clip.setArcHeight(2 * RADIUS);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
        setSkin(createDefaultSkin());
    }

    /**
     * Creates an island showing a content.
     *
     * @param content what the island shows, filling it
     */
    public Island(Node content) {
        this();
        setContent(content);
    }

    /**
     * Gives what the island shows.
     *
     * @return the property of the content, holding {@code null} for none
     */
    public ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /**
     * Gives what the island shows.
     *
     * @return the content, or {@code null} for none
     */
    public @Nullable Node getContent() {
        return content.get();
    }

    /**
     * Changes what the island shows.
     *
     * @param content the content filling the island, or {@code null} for none
     */
    public void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new IslandSkin(this);
    }

    /**
     * Takes no focus: {@inheritDoc}
     */
    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }
}
