package com.chavaillaz.fx.islands.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Stream;

import javafx.application.ColorScheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.chavaillaz.fx.islands.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Base stylesheet")
class BaseStylesheetTest {

    private final SimpleObjectProperty<ColorScheme> system = new SimpleObjectProperty<>(ColorScheme.LIGHT);

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ThemeMode.class, names = {"LIGHT", "DARK"})
    @DisplayName("Writes the prompt of a text field in the secondary color of the text")
    void colorsPrompt(ThemeMode mode) {
        TextField field = new TextField();
        field.setPromptText("Search");
        Scene scene = themed(mode, field);

        assertThat(text(field, "Search")).hasValueSatisfying(
                prompt -> assertThat(prompt.getFill()).isEqualTo(color(scene, "-text-secondary")));
    }

    /**
     * Shows nodes in a scene of a theme, styled and laid out.
     */
    private Scene themed(ThemeMode mode, Node... nodes) {
        VBox root = new VBox(nodes);
        Scene scene = new Scene(root, 600, 400);
        new ThemeManager(mode, system).apply(scene);
        root.applyCss();
        root.layout();
        return scene;
    }

    /**
     * Gives the value a color of the theme takes in a scene, through a region painted with it.
     */
    static Paint color(Scene scene, String name) {
        Region probe = new Region();
        probe.setStyle("-fx-background-color: " + name + ";");
        Pane root = (Pane) scene.getRoot();
        root.getChildren().add(probe);
        probe.applyCss();
        Paint paint = probe.getBackground().getFills().getFirst().getFill();
        root.getChildren().remove(probe);
        return paint;
    }

    /**
     * Finds the text node showing a text among the nodes a parent holds, its skin included.
     */
    static Optional<Text> text(Parent parent, String shown) {
        return descendants(parent)
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .filter(text -> shown.equals(text.getText()))
                .findFirst();
    }

    static Stream<Node> descendants(Parent parent) {
        return parent.getChildrenUnmodifiable().stream()
                .flatMap(child -> child instanceof Parent nested
                        ? Stream.concat(Stream.of(child), descendants(nested))
                        : Stream.of(child));
    }
}
