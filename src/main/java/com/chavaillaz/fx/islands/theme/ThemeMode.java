package com.chavaillaz.fx.islands.theme;

import java.util.Locale;

import javafx.application.ColorScheme;

import com.chavaillaz.fx.islands.internal.Texts;

/**
 * The theme the user chose: the light or the dark one, or the one following the appearance of the operating system.
 */
public enum ThemeMode {

    /** Follows the light or dark appearance of the operating system, switching along with it. */
    SYSTEM,

    /** The light theme, whatever the appearance of the operating system. */
    LIGHT,

    /** The dark theme, whatever the appearance of the operating system. */
    DARK;

    /**
     * Gives the name of the choice, as the menus show it, in the language of the user.
     *
     * @return the name
     */
    public String displayName() {
        return Texts.get("theme." + name().toLowerCase(Locale.ROOT));
    }

    /**
     * Gives the color scheme this choice stands for, whose theme the {@link ThemeManager} applies: the light or the dark
     * one.
     *
     * @param systemColorScheme the color scheme of the operating system, which {@link #SYSTEM} follows
     * @return the color scheme of the theme to apply
     */
    public ColorScheme resolve(ColorScheme systemColorScheme) {
        return switch (this) {
            case SYSTEM -> systemColorScheme == ColorScheme.DARK ? ColorScheme.DARK : ColorScheme.LIGHT;
            case LIGHT -> ColorScheme.LIGHT;
            case DARK -> ColorScheme.DARK;
        };
    }
}
