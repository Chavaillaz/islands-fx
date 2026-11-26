package com.chavaillaz.fx.islands.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.ColorScheme;
import javafx.css.CssParser;
import javafx.css.Stylesheet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Theme")
class ThemeTest {

    /** A color named in a stylesheet, which is neither a property of JavaFX nor a negative number. */
    private static final Pattern COLOR_USED = Pattern.compile("(?<![\\w-])-(?!fx-)[a-z][a-z0-9-]*");
    private static final Pattern COLOR_DEFINED = Pattern.compile("^\\s+(-[a-z][a-z0-9-]*):", Pattern.MULTILINE);

    @ParameterizedTest(name = "{0}")
    @MethodSource("stylesheets")
    @DisplayName("Parses its stylesheets without error")
    void parsesWithoutError(String url) throws IOException {
        CssParser.errorsProperty().clear();
        Stylesheet stylesheet = new CssParser().parse(URI.create(url).toURL());

        assertThat(stylesheet.getRules()).isNotEmpty();
        assertThat(CssParser.errorsProperty()).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("themes")
    @DisplayName("Defines every color the base stylesheet names, and no other one")
    void definesEveryColorNamed(Theme theme) throws IOException {
        Set<String> used = COLOR_USED.matcher(read(Theme.BASE_STYLESHEET)).results()
                .map(match -> match.group())
                .collect(Collectors.toSet());
        Set<String> defined = COLOR_DEFINED.matcher(read(theme.stylesheets().getFirst())).results()
                .map(match -> match.group(1))
                .collect(Collectors.toSet());

        assertThat(defined).isEqualTo(used);
    }

    static Stream<String> stylesheets() {
        return Stream.concat(Stream.of(Theme.BASE_STYLESHEET), themes().flatMap(theme -> theme.stylesheets().stream()));
    }

    static Stream<Theme> themes() {
        return Stream.of(Theme.LIGHT, Theme.DARK);
    }

    @Test
    @DisplayName("Derives a theme overriding some colors of another, of the same color scheme")
    void derivesTheme() {
        Theme contrast = Theme.DARK.derive("contrast", "contrast.css");

        assertThat(contrast.id()).isEqualTo("contrast");
        assertThat(contrast.colorScheme()).isEqualTo(ColorScheme.DARK);
        assertThat(contrast.stylesheets()).containsExactly(Theme.DARK.stylesheets().getFirst(), "contrast.css");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Theme("empty", ColorScheme.LIGHT, List.of()))
                .withMessageContaining("empty");
    }

    private static String read(String url) throws IOException {
        try (InputStream input = URI.create(url).toURL().openStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
