package com.chavaillaz.fx.islands.theme;

import static java.util.Objects.requireNonNullElse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Subscription;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionGroup;
import com.chavaillaz.fx.islands.action.ActionSeparator;
import com.chavaillaz.fx.islands.internal.Texts;

/**
 * Applies the theme the user chose to the windows of the application, and applies it again when they choose another
 * one, or when the operating system switches between light and dark while they follow it.
 * <p>
 * A window is themed through its scene: the stylesheets of the theme and the base stylesheet come first among the
 * stylesheets of the scene, so that a stylesheet added after them can override them, and the color scheme of the scene
 * is the one of the theme, which the window decorations of the operating system follow. The light and the dark themes
 * are the ones of the library unless the application gives its own, and the accent color the one of the theme unless
 * the application chooses another, such as the accent color of the operating system. Popup windows, such as menus
 * and tooltips, take the stylesheets of the window owning them. The alerts of JavaFX show the icons of the library,
 * unless given a graphic of their own, and the texts of the style class {@code monospace} the monospaced font of the
 * operating system.
 * <p>
 * It is used on the JavaFX application thread.
 */
public final class ThemeManager {

    /**
     * The monospaced fonts of the operating systems, from the most to the least preferred: the first one installed
     * shows the texts of the style class {@code monospace}, the generic family {@code monospace} of CSS standing for an
     * old font on most systems, such as Courier.
     */
    static final List<String> MONOSPACED_FONTS = List.of(
            "Menlo", "Cascadia Mono", "Consolas", "DejaVu Sans Mono", "Liberation Mono", "Ubuntu Mono");

    /** The beginning of the stylesheets built for an accent color, or for the monospaced font of the system. */
    private static final String GENERATED_PREFIX = "data:text/css;charset=utf-8;base64,";

    private final ObjectProperty<ThemeMode> mode;
    private final ObjectProperty<Theme> lightTheme = new SimpleObjectProperty<>(this, "lightTheme", Theme.LIGHT);
    private final ObjectProperty<Theme> darkTheme = new SimpleObjectProperty<>(this, "darkTheme", Theme.DARK);
    private final ObjectProperty<@Nullable Color> accent = new SimpleObjectProperty<>(this, "accent");
    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>(this, "theme");
    private final Map<Window, Subscription> windowScenes = new HashMap<>();
    /** The stylesheets this manager may have put first in a scene, which it takes out before putting the current ones. */
    private final Set<String> managedStylesheets = new LinkedHashSet<>(List.of(
            Theme.LIGHT.stylesheets().getFirst(), Theme.DARK.stylesheets().getFirst(), Theme.BASE_STYLESHEET));

    /**
     * Creates a manager following the appearance of the operating system when the user chooses to.
     *
     * @param mode the theme the user chose
     */
    public ThemeManager(ThemeMode mode) {
        this(mode, Platform.getPreferences().colorSchemeProperty());
    }

    /**
     * Creates a manager following the given color scheme as the appearance of the operating system.
     *
     * @param mode              the theme the user chose
     * @param systemColorScheme the color scheme of the operating system, followed by {@link ThemeMode#SYSTEM}
     */
    public ThemeManager(ThemeMode mode, ObservableValue<ColorScheme> systemColorScheme) {
        this.mode = new SimpleObjectProperty<>(this, "mode", mode);
        theme.bind(Bindings.createObjectBinding(
                () -> requireNonNullElse(this.mode.get(), ThemeMode.SYSTEM).resolve(systemColorScheme.getValue())
                        == ColorScheme.DARK ? darkTheme.get() : lightTheme.get(),
                this.mode, systemColorScheme, lightTheme, darkTheme));
    }

    /**
     * Gives the theme the user chose, which the theme applied follows.
     *
     * @return the property of the choice
     */
    public ObjectProperty<ThemeMode> modeProperty() {
        return mode;
    }

    /**
     * Gives the theme the user chose.
     *
     * @return the choice
     */
    public ThemeMode getMode() {
        return mode.get();
    }

    /**
     * Changes the theme the user chose, applying the theme it stands for.
     *
     * @param mode the choice
     */
    public void setMode(ThemeMode mode) {
        this.mode.set(mode);
    }

    /**
     * Gives the theme applied while the light one is chosen, or followed in the appearance of the operating system.
     *
     * @return the property of the light theme, the one of the library unless the application gives its own
     */
    public ObjectProperty<Theme> lightThemeProperty() {
        return lightTheme;
    }

    /**
     * Gives the theme applied while the light one is chosen.
     *
     * @return the light theme
     */
    public Theme getLightTheme() {
        return lightTheme.get();
    }

    /**
     * Changes the theme applied while the light one is chosen, such as a theme derived from {@link Theme#LIGHT}.
     *
     * @param theme the light theme
     */
    public void setLightTheme(Theme theme) {
        lightTheme.set(theme);
    }

    /**
     * Gives the theme applied while the dark one is chosen, or followed in the appearance of the operating system.
     *
     * @return the property of the dark theme, the one of the library unless the application gives its own
     */
    public ObjectProperty<Theme> darkThemeProperty() {
        return darkTheme;
    }

    /**
     * Gives the theme applied while the dark one is chosen.
     *
     * @return the dark theme
     */
    public Theme getDarkTheme() {
        return darkTheme.get();
    }

    /**
     * Changes the theme applied while the dark one is chosen, such as a theme derived from {@link Theme#DARK}.
     *
     * @param theme the dark theme
     */
    public void setDarkTheme(Theme theme) {
        darkTheme.set(theme);
    }

    /**
     * Gives the accent color of the controls, which the selections and the selected tabs are tinted with as well. It
     * can be bound to the accent color of the operating system, {@code Platform.getPreferences().accentColorProperty()}.
     *
     * @return the property of the accent, holding {@code null} while the theme gives its own
     */
    public ObjectProperty<@Nullable Color> accentProperty() {
        return accent;
    }

    /**
     * Gives the accent color of the controls.
     *
     * @return the accent, or {@code null} while the theme gives its own
     */
    public @Nullable Color getAccent() {
        return accent.get();
    }

    /**
     * Changes the accent color of the controls.
     *
     * @param accent the accent, or {@code null} for the one of the theme
     */
    public void setAccent(@Nullable Color accent) {
        this.accent.set(accent);
    }

    /**
     * Gives the theme applied, resolved from the choice of the user and the appearance of the operating system.
     *
     * @return the property of the theme
     */
    public ReadOnlyObjectProperty<Theme> themeProperty() {
        return theme.getReadOnlyProperty();
    }

    /**
     * Gives the theme applied.
     *
     * @return the theme
     */
    public Theme getTheme() {
        return theme.get();
    }

    /**
     * Creates the menu choosing the theme: light, dark, or following the appearance of the operating system, the
     * choice of the user being checked.
     *
     * @return the group of the actions choosing each theme, titled {@code Theme} in English
     */
    public ActionGroup createMenu() {
        return ActionGroup.of(Texts.get("theme.menu"),
                createChoice(ThemeMode.LIGHT),
                createChoice(ThemeMode.DARK),
                ActionSeparator.INSTANCE,
                createChoice(ThemeMode.SYSTEM));
    }

    /**
     * Applies the theme to the windows showing, to those shown afterwards, and again to all of them whenever the
     * theme changes, until unsubscribed. It is called once, before the first window shows.
     *
     * @return the subscription, which stops applying the theme to the windows once unsubscribed
     */
    public Subscription install() {
        ListChangeListener<Window> windowsListener = change -> {
            while (change.next()) {
                change.getRemoved().forEach(this::forget);
                change.getAddedSubList().forEach(this::follow);
            }
        };
        Window.getWindows().addListener(windowsListener);
        Window.getWindows().forEach(this::follow);
        Subscription themeSubscription = theme.subscribe((previous, current) ->
                Window.getWindows().forEach(window -> apply(window.getScene())))
                .and(accent.subscribe((previous, current) ->
                        Window.getWindows().forEach(window -> apply(window.getScene()))));
        return () -> {
            Window.getWindows().removeListener(windowsListener);
            themeSubscription.unsubscribe();
            List.copyOf(windowScenes.keySet()).forEach(this::forget);
        };
    }

    /**
     * Applies the theme to a scene, which a window can be given before it shows, so that it never shows unthemed.
     *
     * @param scene the scene, nothing being done without one
     */
    public void apply(@Nullable Scene scene) {
        if (scene == null) {
            return;
        }
        Theme current = getTheme();
        List<String> themed = new ArrayList<>(current.stylesheets());
        Color color = getAccent();
        if (color != null) {
            themed.add(accentStylesheet(color, current.colorScheme()));
        }
        themed.add(Theme.BASE_STYLESHEET);
        String monospace = monospaceStylesheet();
        if (monospace != null) {
            themed.add(monospace);
        }
        managedStylesheets.addAll(current.stylesheets());
        List<String> stylesheets = new ArrayList<>(scene.getStylesheets());
        stylesheets.removeIf(url -> managedStylesheets.contains(url) || url.startsWith(GENERATED_PREFIX));
        stylesheets.addAll(0, themed);
        if (!stylesheets.equals(scene.getStylesheets())) {
            scene.getStylesheets().setAll(stylesheets);
        }
        scene.getPreferences().setColorScheme(current.colorScheme());
        if (scene.getRoot() instanceof DialogPane pane) {
            DialogIcons.decorate(pane);
        }
    }

    /**
     * Builds the stylesheet giving an accent color, and the colors tinted with it: the selection, as the text selected,
     * and the selected tabs, seen through over the islands, more so in the dark scheme.
     */
    static String accentStylesheet(Color accent, ColorScheme scheme) {
        boolean dark = scheme == ColorScheme.DARK;
        String css = ".root {\n"
                + "    -accent: " + rgba(accent, 1) + ";\n"
                + "    -selection-active: " + rgba(accent, dark ? 0.48 : 0.23) + ";\n"
                + "    -tab-selected-bg: " + rgba(accent, dark ? 0.31 : 0.14) + ";\n"
                + "    -tab-selected-border: " + rgba(accent, dark ? 0.6 : 0.42) + ";\n"
                + "}\n";
        return stylesheet(css);
    }

    /**
     * Gives the stylesheet showing the texts of the style class {@code monospace} in the monospaced font of the
     * operating system, found the first time a scene is themed.
     *
     * @return the URL of the stylesheet, or {@code null} when none of the {@link #MONOSPACED_FONTS} is installed
     */
    static @Nullable String monospaceStylesheet() {
        return MonospaceStylesheet.URL;
    }

    /**
     * Builds the stylesheet showing the texts of the style class {@code monospace} in the first of the
     * {@link #MONOSPACED_FONTS} installed.
     *
     * @param families the families of the fonts installed
     * @return the URL of the stylesheet, or {@code null} when none of the fonts is installed
     */
    static @Nullable String monospaceStylesheet(Collection<String> families) {
        return MONOSPACED_FONTS.stream()
                .filter(families::contains)
                .findFirst()
                .map(font -> stylesheet(".monospace {\n    -fx-font-family: \"" + font + "\";\n}\n"))
                .orElse(null);
    }

    private static String stylesheet(String css) {
        return GENERATED_PREFIX + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }

    private static String rgba(Color color, double opacity) {
        return String.format(Locale.ROOT, "rgba(%d, %d, %d, %.2f)", Math.round(color.getRed() * 255),
                Math.round(color.getGreen() * 255), Math.round(color.getBlue() * 255), opacity);
    }

    private Action createChoice(ThemeMode choice) {
        return Action.builder("theme." + choice.name().toLowerCase(Locale.ROOT), choice.displayName())
                .selected(mode.isEqualTo(choice))
                .handler(() -> setMode(choice))
                .build();
    }

    /**
     * Applies the theme to the scene of a window, and to the scene it is given afterwards while it shows. A popup
     * window is left alone, taking the stylesheets of the window owning it.
     */
    private void follow(Window window) {
        if (!(window instanceof PopupWindow) && !windowScenes.containsKey(window)) {
            windowScenes.put(window, window.sceneProperty().subscribe(this::apply));
        }
    }

    private void forget(Window window) {
        Subscription subscription = windowScenes.remove(window);
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /**
     * Holds the stylesheet naming the monospaced font of the system, found once JavaFX lists the fonts installed.
     */
    private static final class MonospaceStylesheet {

        private static final @Nullable String URL = monospaceStylesheet(Font.getFamilies());
    }
}
