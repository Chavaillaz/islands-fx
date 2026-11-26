package com.chavaillaz.fx.islands.icon;

import javafx.beans.NamedArg;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import org.jspecify.annotations.Nullable;

/**
 * Shows an {@link Icon} at a size, scaling the outline drawn on its grid of {@link #GRID} pixels.
 * <p>
 * The view carries the style class {@code icon} and the one of its icon, and its outline the style class
 * {@code icon-shape}, whose stroke the stylesheets set. It lets the mouse through, to the control showing it.
 * <p>
 * An FXML file declares it as {@code <IconView icon="SAVE" size="20"/>}, naming an icon of the library, or with
 * {@code fx:constant} for an icon of the application.
 */
public final class IconView extends Region {

    /**
     * The size of the grid the icons are drawn on, in pixels, at which an icon is shown unless told otherwise.
     */
    public static final double GRID = 16;

    private final SVGPath shape = new SVGPath();
    private final Scale scale = new Scale(1, 1, 0, 0);
    private final ObjectProperty<@Nullable Icon> icon = new SimpleObjectProperty<>(this, "icon");
    private final DoubleProperty size = new SimpleDoubleProperty(this, "size", GRID);

    /**
     * Creates a view showing no icon yet, at the size of the grid.
     */
    public IconView() {
        shape.getStyleClass().add("icon-shape");
        shape.getTransforms().add(scale);
        getChildren().add(shape);
        getStyleClass().add("icon");
        setMouseTransparent(true);
        icon.subscribe((previous, current) -> {
            if (previous != null) {
                getStyleClass().remove(previous.styleClass());
            }
            if (current != null) {
                getStyleClass().add(current.styleClass());
            }
            shape.setContent(current == null ? "" : current.path());
        });
        size.subscribe(value -> {
            double pixels = value.doubleValue();
            scale.setX(pixels / GRID);
            scale.setY(pixels / GRID);
            setMinSize(pixels, pixels);
            setPrefSize(pixels, pixels);
            setMaxSize(pixels, pixels);
        });
    }

    /**
     * Creates a view showing an icon at the size of its grid.
     *
     * @param icon the icon shown
     */
    public IconView(@NamedArg("icon") Icon icon) {
        this(icon, GRID);
    }

    /**
     * Creates a view showing an icon at a size.
     *
     * @param icon the icon shown
     * @param size the width and height of the view in pixels, the icon being scaled from its grid to fill it
     */
    public IconView(@NamedArg("icon") Icon icon, @NamedArg(value = "size", defaultValue = "16") double size) {
        this();
        setIcon(icon);
        setSize(size);
    }

    /**
     * Gives the icon shown.
     *
     * @return the property of the icon, holding {@code null} while the view shows none
     */
    public ObjectProperty<@Nullable Icon> iconProperty() {
        return icon;
    }

    /**
     * Gives the icon shown.
     *
     * @return the icon, or {@code null} for none
     */
    public @Nullable Icon getIcon() {
        return icon.get();
    }

    /**
     * Changes the icon shown.
     *
     * @param icon the icon, or {@code null} for none
     */
    public void setIcon(@Nullable Icon icon) {
        this.icon.set(icon);
    }

    /**
     * Gives the width and height of the view, the icon being scaled from its grid to fill it.
     *
     * @return the property of the size in pixels
     */
    public DoubleProperty sizeProperty() {
        return size;
    }

    /**
     * Gives the width and height of the view.
     *
     * @return the size in pixels
     */
    public double getSize() {
        return size.get();
    }

    /**
     * Changes the width and height of the view.
     *
     * @param size the size in pixels
     */
    public void setSize(double size) {
        this.size.set(size);
    }
}
