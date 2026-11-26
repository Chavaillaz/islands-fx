package com.chavaillaz.fx.islands.theme;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.application.ColorScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("ThemeMode")
class ThemeModeTest {

    @ParameterizedTest(name = "{0} gives {2} when the system is {1}")
    @CsvSource({
            "SYSTEM, DARK, DARK",
            "SYSTEM, LIGHT, LIGHT",
            "LIGHT, DARK, LIGHT",
            "DARK, LIGHT, DARK"
    })
    @DisplayName("Follows the operating system only when chosen to")
    void followsSystemOnlyWhenChosen(ThemeMode mode, ColorScheme system, ColorScheme expected) {
        assertThat(mode.resolve(system)).isEqualTo(expected);
    }
}
