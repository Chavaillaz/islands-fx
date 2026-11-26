package com.chavaillaz.fx.islands.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.List;

import javafx.application.ColorScheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Subscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionGroup;

@ExtendWith(JavaFxExtension.class)
@DisplayName("ThemeManager")
class ThemeManagerTest {

    private final SimpleObjectProperty<ColorScheme> system = new SimpleObjectProperty<>(ColorScheme.LIGHT);

    @Test
    @DisplayName("Puts the stylesheets of the theme first, before those of the scene")
    void putsThemeStylesheetsFirst() {
        Scene scene = new Scene(new StackPane());
        scene.getStylesheets().add("feature.css");
        new ThemeManager(ThemeMode.DARK, system).apply(scene);

        assertThat(scene.getStylesheets())
                .containsExactly(Theme.DARK.stylesheets().getFirst(), Theme.BASE_STYLESHEET, "feature.css");
        assertThat(scene.getPreferences().getColorScheme()).isEqualTo(ColorScheme.DARK);
    }

    @Test
    @DisplayName("Replaces the stylesheet of the theme applied before")
    void replacesThemeAppliedBefore() {
        Scene scene = new Scene(new StackPane());
        ThemeManager themes = new ThemeManager(ThemeMode.DARK, system);
        themes.apply(scene);
        themes.setMode(ThemeMode.LIGHT);
        themes.apply(scene);

        assertThat(scene.getStylesheets()).containsExactly(Theme.LIGHT.stylesheets().getFirst(), Theme.BASE_STYLESHEET);
        assertThat(scene.getPreferences().getColorScheme()).isEqualTo(ColorScheme.LIGHT);
    }

    @Test
    @DisplayName("Follows the appearance of the operating system while chosen to")
    void followsOperatingSystem() {
        ThemeManager themes = new ThemeManager(ThemeMode.SYSTEM, system);
        assertThat(themes.getTheme()).isEqualTo(Theme.LIGHT);

        system.set(ColorScheme.DARK);
        assertThat(themes.getTheme()).isEqualTo(Theme.DARK);

        themes.setMode(ThemeMode.LIGHT);
        assertThat(themes.getTheme()).isEqualTo(Theme.LIGHT);
        assertThat(themes.getMode()).isEqualTo(ThemeMode.LIGHT);
    }

    @Test
    @DisplayName("Themes the windows showing, and themes them again when the theme changes")
    void themesWindowsShowing() {
        ThemeManager themes = new ThemeManager(ThemeMode.LIGHT, system);
        Subscription installation = themes.install();
        Stage stage = new Stage();
        stage.setScene(new Scene(new StackPane(), 200, 100));
        stage.show();
        try {
            assertThat(stage.getScene().getStylesheets()).startsWith(Theme.LIGHT.stylesheets().getFirst());

            themes.modeProperty().set(ThemeMode.DARK);
            assertThat(stage.getScene().getStylesheets()).startsWith(Theme.DARK.stylesheets().getFirst());

            stage.setScene(new Scene(new StackPane(), 200, 100));
            assertThat(stage.getScene().getStylesheets()).startsWith(Theme.DARK.stylesheets().getFirst());

            stage.hide();
            stage.show();
            installation.unsubscribe();
            themes.setMode(ThemeMode.LIGHT);
            stage.setScene(new Scene(new StackPane(), 200, 100));
            assertThat(stage.getScene().getStylesheets()).isEmpty();
        } finally {
            installation.unsubscribe();
            stage.hide();
        }
    }

    @Test
    @DisplayName("Does nothing to a window without scene")
    void ignoresMissingScene() {
        ThemeManager themes = new ThemeManager(ThemeMode.LIGHT, system);
        assertThatNoException().isThrownBy(() -> themes.apply(null));
    }

    @Test
    @DisplayName("Chooses the theme from a menu checking the choice of the user")
    void choosesThemeFromMenu() {
        ThemeManager themes = new ThemeManager(ThemeMode.SYSTEM, system);
        ActionGroup menu = themes.createMenu();
        List<Action> choices = menu.elements().stream()
                .filter(Action.class::isInstance)
                .map(Action.class::cast)
                .toList();

        assertThat(menu.text()).isEqualTo("Theme");
        assertThat(choices).extracting(Action::getText).containsExactly("Light", "Dark", "Sync with OS");
        assertThat(choices).extracting(Action::isSelected).containsExactly(false, false, true);

        choices.get(1).perform();
        assertThat(themes.getMode()).isEqualTo(ThemeMode.DARK);
        assertThat(choices).extracting(Action::isSelected).containsExactly(false, true, false);
    }

    @Test
    @DisplayName("Applies the light and the dark themes the application gives")
    void appliesThemesOfApplication() {
        Scene scene = new Scene(new StackPane());
        ThemeManager themes = new ThemeManager(ThemeMode.SYSTEM, system);
        Theme contrast = Theme.DARK.derive("contrast", "contrast.css");
        themes.setDarkTheme(contrast);
        system.set(ColorScheme.DARK);
        themes.apply(scene);

        assertThat(themes.getTheme()).isSameAs(contrast);
        assertThat(scene.getStylesheets())
                .containsExactly(Theme.DARK.stylesheets().getFirst(), "contrast.css", Theme.BASE_STYLESHEET);

        themes.setDarkTheme(Theme.DARK);
        themes.apply(scene);
        assertThat(scene.getStylesheets()).containsExactly(Theme.DARK.stylesheets().getFirst(), Theme.BASE_STYLESHEET);
        assertThat(themes.getLightTheme()).isSameAs(Theme.LIGHT);
        assertThat(themes.getDarkTheme()).isSameAs(Theme.DARK);
    }

    @Test
    @DisplayName("Paints the accent the application chooses, and the one of the theme again once unset")
    void paintsAccentChosen() {
        StackPane root = new StackPane();
        Scene scene = new Scene(root);
        ThemeManager themes = new ThemeManager(ThemeMode.DARK, system);
        Color green = Color.web("#3F9F6A");
        themes.setAccent(green);
        themes.apply(scene);

        assertThat(themes.getAccent()).isEqualTo(green);
        assertThat(scene.getStylesheets()).hasSize(3);
        assertThat(BaseStylesheetTest.color(scene, "-accent")).isEqualTo(green);
        assertThat(BaseStylesheetTest.color(scene, "-selection-active"))
                .isEqualTo(Color.web("#3F9F6A", 0.48));

        themes.setAccent(null);
        themes.apply(scene);
        assertThat(scene.getStylesheets()).containsExactly(Theme.DARK.stylesheets().getFirst(), Theme.BASE_STYLESHEET);
        assertThat(BaseStylesheetTest.color(scene, "-accent")).isEqualTo(Color.web("#3871E1"));
    }
}
