package com.chavaillaz.fx.islands.theme;

import java.net.URL;
import java.util.Objects;

import javafx.application.ColorScheme;

/**
 * The color themes of the application, a dark and a light one.
 * <p>
 * A theme is a stylesheet defining the colors that the {@link #BASE_STYLESHEET base stylesheet} styles the controls
 * with, whatever the theme. A color added to the base stylesheet is therefore defined by every theme.
 */
public enum Theme {

    /** Light text on dark islands. */
    DARK("dark.css", ColorScheme.DARK),

    /** Dark text on white islands. */
    LIGHT("light.css", ColorScheme.LIGHT);

    /**
     * The URL of the stylesheet styling the controls with the colors a theme defines.
     */
    public static final String BASE_STYLESHEET = resource("base.css");

    private final String stylesheet;
    private final ColorScheme colorScheme;

    Theme(String stylesheet, ColorScheme colorScheme) {
        this.stylesheet = resource(stylesheet);
        this.colorScheme = colorScheme;
    }

    /**
     * Gives the stylesheet defining the colors of the theme.
     *
     * @return the URL of the stylesheet
     */
    public String stylesheet() {
        return stylesheet;
    }

    /**
     * Gives the color scheme of the theme, which the window decorations of the operating system follow.
     *
     * @return dark or light
     */
    public ColorScheme colorScheme() {
        return colorScheme;
    }

    private static String resource(String name) {
        URL url = Objects.requireNonNull(Theme.class.getResource(name), () -> "No stylesheet named " + name);
        return url.toExternalForm();
    }
}
