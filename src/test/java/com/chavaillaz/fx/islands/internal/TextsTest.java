package com.chavaillaz.fx.islands.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Texts")
class TextsTest {

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"messages_fr.properties", "messages_de.properties"})
    @DisplayName("Translates every text of the library")
    void translatesEveryText(String file) throws IOException {
        assertThat(read(file).stringPropertyNames()).isEqualTo(read("messages.properties").stringPropertyNames());
    }

    @Test
    @DisplayName("Speaks the language of the user, English when the library does not speak it")
    void speaksLanguageOfUser() {
        Locale former = Locale.getDefault(Locale.Category.DISPLAY);
        Locale formerDefault = Locale.getDefault();
        try {
            Locale.setDefault(Locale.Category.DISPLAY, Locale.FRENCH);
            assertThat(Texts.get("toolWindow.hide")).isEqualTo("Masquer");
            assertThat(Texts.format("toolWindow.toggle", "Commandes"))
                    .isEqualTo("Affiche ou masque la fenêtre d'outils Commandes");

            Locale.setDefault(Locale.GERMAN);
            Locale.setDefault(Locale.Category.DISPLAY, Locale.JAPANESE);
            assertThat(Texts.get("toolWindow.hide")).isEqualTo("Hide");
        } finally {
            Locale.setDefault(formerDefault);
            Locale.setDefault(Locale.Category.DISPLAY, former);
        }
    }

    private static Properties read(String file) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Texts.class.getResourceAsStream(file);
             Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
