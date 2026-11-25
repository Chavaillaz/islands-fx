package com.chavaillaz.fx.islands.workbench;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.jspecify.annotations.Nullable;

/**
 * A panel with rounded corners standing out of the background of the window, holding a tool window or the content of
 * the window: its content is clipped to its corners.
 * <p>
 * It carries the style class {@code island}, whose background the stylesheets round by {@link #RADIUS} as well.
 */
public final class Island extends StackPane {

    /** The radius of the corners in pixels, which the stylesheets give the background of an island too. */
    public static final double RADIUS = 10;

    /**
     * Creates an island showing nothing yet.
     */
    public Island() {
        getStyleClass().add("island");
        Rectangle clip = new Rectangle();
        clip.setArcWidth(2 * RADIUS);
        clip.setArcHeight(2 * RADIUS);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
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
     * @return the content, or {@code null} for none
     */
    public @Nullable Node getContent() {
        return getChildren().isEmpty() ? null : getChildren().getFirst();
    }

    /**
     * Changes what the island shows.
     *
     * @param content the content filling the island, or {@code null} for none
     */
    public void setContent(@Nullable Node content) {
        if (content == null) {
            getChildren().clear();
        } else {
            getChildren().setAll(content);
        }
    }
}
