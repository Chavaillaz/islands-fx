package com.chavaillaz.fx.islands.icon;

import java.util.Locale;

/**
 * An icon, drawn as outlines on a grid of 16 by 16 pixels.
 * <p>
 * An icon is an SVG path, which its {@link IconView view} strokes in the color the stylesheets give the style class
 * {@code icon-shape}. Each icon also carries a style class of its own, {@code icon-} followed by its name in lowercase
 * (such as {@code icon-object}), through which the stylesheets color it differently. {@link Icons} holds the icons of
 * the library; an application adds its own with an enumeration implementing this interface.
 */
public interface Icon {

    /**
     * Gives the name of the icon, unique among the icons of the application.
     *
     * @return the name, in uppercase with underscores, as the constant of an enumeration
     */
    String name();

    /**
     * Gives the SVG path drawing the icon on its grid of 16 by 16 pixels.
     *
     * @return the path, in the syntax of the {@code d} attribute of an SVG path
     */
    String path();

    /**
     * Gives the style class of the icon, through which the stylesheets can color it.
     *
     * @return {@code icon-} followed by the name of the icon in lowercase, with dashes for underscores
     */
    default String styleClass() {
        return "icon-" + name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
