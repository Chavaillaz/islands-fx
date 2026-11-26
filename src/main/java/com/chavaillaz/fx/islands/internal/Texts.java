package com.chavaillaz.fx.islands.internal;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Gives the texts the library shows, in the language of the user: English, French or German, after the locale of the
 * display, English otherwise.
 * <p>
 * A text is looked up each time a control is made, so that a locale changed before is followed.
 */
public final class Texts {

    private static final String BUNDLE = "com.chavaillaz.fx.islands.internal.messages";

    private Texts() {
    }

    /**
     * Gives a text in the language of the user.
     *
     * @param key the key of the text
     * @return the text
     */
    public static String get(String key) {
        return bundle().getString(key);
    }

    /**
     * Gives a text in the language of the user, its placeholders, such as {@code {0}}, replaced by the given values.
     *
     * @param key    the key of the text
     * @param values the values of the placeholders, in their order
     * @return the text
     */
    public static String format(String key, Object... values) {
        return new MessageFormat(get(key), locale()).format(values);
    }

    /**
     * Gives the texts of the language of the display, or the English ones: rather than those of the default locale,
     * which the lookup falls back to for a language the library does not speak.
     */
    private static ResourceBundle bundle() {
        Locale locale = locale();
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);
        return bundle.getLocale().getLanguage().equals(locale.getLanguage())
                ? bundle
                : ResourceBundle.getBundle(BUNDLE, Locale.ROOT);
    }

    private static Locale locale() {
        return Locale.getDefault(Locale.Category.DISPLAY);
    }
}
