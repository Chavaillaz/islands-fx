package com.chavaillaz.fx.islands.theme;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.application.ColorScheme;

/**
 * A color theme: the stylesheets defining the colors that the {@link #BASE_STYLESHEET base stylesheet} styles the
 * controls with, whatever the theme. The library gives a {@link #LIGHT light} and a {@link #DARK dark} one, from which
 * an application derives its own, overriding some of their colors, such as a theme of higher contrast or the colors of
 * its brand, which the {@link ThemeManager} then applies.
 * <p>
 * The stylesheets of a theme define every color the base stylesheet names, as the built-in ones do: a color added to
 * the base stylesheet is therefore defined by every theme.
 *
 * @param id          the identifier of the theme, such as {@code dark}
 * @param colorScheme the color scheme of the theme, which the window decorations of the operating system follow
 * @param stylesheets the URLs of the stylesheets defining the colors of the theme, each overriding those before it
 */
public record Theme(String id, ColorScheme colorScheme, List<String> stylesheets) {

    /**
     * The URL of the stylesheet styling the controls with the colors a theme defines.
     */
    public static final String BASE_STYLESHEET = resource("base.css");

    /** Dark text on white islands. */
    public static final Theme LIGHT = new Theme("light", ColorScheme.LIGHT, List.of(resource("light.css")));

    /** Light text on dark islands. */
    public static final Theme DARK = new Theme("dark", ColorScheme.DARK, List.of(resource("dark.css")));

    /**
     * Creates a theme, copying its stylesheets.
     *
     * @param id          the identifier of the theme, such as {@code dark}
     * @param colorScheme the color scheme of the theme, which the window decorations of the operating system follow
     * @param stylesheets the URLs of the stylesheets defining the colors of the theme, each overriding those before it
     * @throws IllegalArgumentException when no stylesheet is given
     */
    public Theme {
        requireNonNull(id, "id");
        requireNonNull(colorScheme, "colorScheme");
        stylesheets = List.copyOf(stylesheets);
        if (stylesheets.isEmpty()) {
            throw new IllegalArgumentException("The theme " + id + " has no stylesheet");
        }
    }

    /**
     * Derives a theme from this one, a stylesheet overriding some of its colors, such as {@code -accent}.
     *
     * @param id         the identifier of the theme derived
     * @param stylesheet the URL of the stylesheet overriding colors, applied after those of this theme
     * @return the theme derived, of the same color scheme
     */
    public Theme derive(String id, String stylesheet) {
        List<String> derived = new ArrayList<>(stylesheets);
        derived.add(requireNonNull(stylesheet, "stylesheet"));
        return new Theme(id, colorScheme, derived);
    }

    private static String resource(String name) {
        URL url = Objects.requireNonNull(Theme.class.getResource(name), () -> "No stylesheet named " + name);
        return url.toExternalForm();
    }
}
