package com.chavaillaz.fx.islands.theme;

import static java.util.Objects.requireNonNullElse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

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
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import javafx.util.Subscription;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionGroup;
import com.chavaillaz.fx.islands.action.ActionSeparator;

/**
 * Applies the theme the user chose to the windows of the application, and applies it again when they choose another
 * one, or when the operating system switches between light and dark while they follow it.
 * <p>
 * A window is themed through its scene: the stylesheet of the theme and the base stylesheet come first among the
 * stylesheets of the scene, so that a stylesheet added after them can override them, and the color scheme of the scene
 * is the one of the theme, which the window decorations of the operating system follow. Popup windows, such as menus
 * and tooltips, take the stylesheets of the window owning them. The alerts of JavaFX show the icons of the library,
 * unless given a graphic of their own.
 * <p>
 * It is used on the JavaFX application thread.
 */
public final class ThemeManager {

    private static final List<String> THEME_STYLESHEETS = Stream.concat(
                    Arrays.stream(Theme.values()).map(Theme::stylesheet),
                    Stream.of(Theme.BASE_STYLESHEET))
            .toList();

    private final ObjectProperty<ThemeMode> mode;
    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>(this, "theme");
    private final Map<Window, Subscription> windowScenes = new HashMap<>();

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
                () -> requireNonNullElse(this.mode.get(), ThemeMode.SYSTEM).resolve(systemColorScheme.getValue()),
                this.mode, systemColorScheme));
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
     * @return the group of the actions choosing each theme, titled {@code Theme}
     */
    public ActionGroup createMenu() {
        return ActionGroup.of("Theme",
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
                Window.getWindows().forEach(window -> apply(window.getScene())));
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
        List<String> stylesheets = new ArrayList<>(scene.getStylesheets());
        stylesheets.removeAll(THEME_STYLESHEETS);
        stylesheets.addAll(0, List.of(current.stylesheet(), Theme.BASE_STYLESHEET));
        if (!stylesheets.equals(scene.getStylesheets())) {
            scene.getStylesheets().setAll(stylesheets);
        }
        scene.getPreferences().setColorScheme(current.colorScheme());
        if (scene.getRoot() instanceof DialogPane pane) {
            DialogIcons.decorate(pane);
        }
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
}
