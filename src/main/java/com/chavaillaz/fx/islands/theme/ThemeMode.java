package com.chavaillaz.fx.islands.theme;

import javafx.application.ColorScheme;

/**
 * The theme the user chose: one of their own, or the one following the appearance of the operating system.
 */
public enum ThemeMode {

    /** Follows the light or dark appearance of the operating system, switching along with it. */
    SYSTEM("Sync with OS"),

    /** The light theme, whatever the appearance of the operating system. */
    LIGHT("Light"),

    /** The dark theme, whatever the appearance of the operating system. */
    DARK("Dark");

    private final String displayName;

    ThemeMode(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gives the name of the choice, as the menus show it.
     *
     * @return the name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Gives the theme this choice stands for.
     *
     * @param systemColorScheme the color scheme of the operating system, which {@link #SYSTEM} follows
     * @return the theme to apply
     */
    public Theme resolve(ColorScheme systemColorScheme) {
        return switch (this) {
            case SYSTEM -> systemColorScheme == ColorScheme.DARK ? Theme.DARK : Theme.LIGHT;
            case LIGHT -> Theme.LIGHT;
            case DARK -> Theme.DARK;
        };
    }
}
