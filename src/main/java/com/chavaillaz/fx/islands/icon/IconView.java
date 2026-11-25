package com.chavaillaz.fx.islands.icon;

import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/**
 * Shows an {@link Icon} at a size, scaling the outline drawn on its grid of {@link #GRID} pixels.
 * <p>
 * The view carries the style class {@code icon} and the one of its icon, and its outline the style class
 * {@code icon-shape}, whose stroke the stylesheets set. It lets the mouse through, to the control showing it.
 */
public final class IconView extends Region {

    /**
     * The size of the grid the icons are drawn on, in pixels, at which an icon is shown unless told otherwise.
     */
    public static final double GRID = 16;

    private final Icon icon;

    /**
     * Creates a view showing an icon at the size of its grid.
     *
     * @param icon the icon shown
     */
    public IconView(Icon icon) {
        this(icon, GRID);
    }

    /**
     * Creates a view showing an icon at a size.
     *
     * @param icon the icon shown
     * @param size the width and height of the view in pixels, the icon being scaled from its grid to fill it
     */
    public IconView(Icon icon, double size) {
        this.icon = icon;
        SVGPath shape = new SVGPath();
        shape.setContent(icon.path());
        shape.getStyleClass().add("icon-shape");
        if (size != GRID) {
            shape.getTransforms().add(new Scale(size / GRID, size / GRID, 0, 0));
        }
        getChildren().add(shape);
        getStyleClass().addAll("icon", icon.styleClass());
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        setMouseTransparent(true);
    }

    /**
     * Gives the icon shown.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }
}
