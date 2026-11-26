package com.chavaillaz.fx.islands.showcase;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

import javafx.application.ColorScheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.control.Tone;
import com.chavaillaz.fx.islands.theme.ThemeManager;
import com.chavaillaz.fx.islands.theme.ThemeMode;
import com.chavaillaz.fx.islands.workbench.Workbenches;

/**
 * Renders the showcase in the light theme and the dark one, its main window with the main menu in its toolbar. Given
 * a folder ({@code -Dshowcase.screenshots=docs/screenshots}), the renders are written there as the screenshots of the
 * README, at twice their size for the screens of high density.
 */
@ExtendWith(JavaFxExtension.class)
@DisplayName("Showcase")
class ShowcaseTest {

    private static final String SCREENSHOTS = System.getProperty("showcase.screenshots", "");
    private static final double SCALE = SCREENSHOTS.isEmpty() ? 1 : 2;

    private static ThemeManager themes(ThemeMode mode) {
        return new ThemeManager(mode, new SimpleObjectProperty<>(ColorScheme.LIGHT));
    }

    /**
     * Renders the window, leaving out what goes beyond its edges, such as the shadows of the notifications. A first
     * rendering lays out the nodes that skins add while laid out, such as the buttons of a pagination, for the second
     * one to show them styled.
     */
    private static WritableImage render(Scene scene) {
        scene.getRoot().snapshot(null, null);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(Transform.scale(SCALE, SCALE));
        parameters.setViewport(new Rectangle2D(0, 0, scene.getWidth() * SCALE, scene.getHeight() * SCALE));
        return scene.getRoot().snapshot(parameters, null);
    }

    private static void save(WritableImage image, String name, ThemeMode mode) throws IOException {
        if (!SCREENSHOTS.isEmpty()) {
            Path folder = Path.of(SCREENSHOTS);
            Files.createDirectories(folder);
            Png.write(image, folder.resolve(name + '-' + mode.name().toLowerCase(Locale.ROOT) + ".png"));
        }
    }

    private static Stream<Node> descendants(Node node) {
        if (node instanceof Parent parent) {
            return Stream.concat(Stream.of(node),
                    parent.getChildrenUnmodifiable().stream().flatMap(ShowcaseTest::descendants));
        }
        return Stream.of(node);
    }

    private static Stream<String> texts(Scene scene, String selector) {
        return scene.getRoot().lookupAll(selector).stream()
                .flatMap(ShowcaseTest::descendants)
                .filter(Labeled.class::isInstance)
                .map(node -> ((Labeled) node).getText());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ThemeMode.class, names = {"LIGHT", "DARK"})
    @DisplayName("Shows the main window of an application, notifying a failure or searching its actions")
    void showsMainWindow(ThemeMode mode) throws IOException {
        Showcase showcase = new Showcase(Workbenches.withMenuInToolbar(), themes(mode));
        Stage stage = new Stage();
        stage.setWidth(Showcase.WIDTH);
        stage.setHeight(Showcase.HEIGHT);
        Scene scene = showcase.createScene(stage);
        stage.show();
        try {
            showcase.getStatus().running("Synchronizing the orders…", 0.6);
            if (mode == ThemeMode.DARK) {
                showcase.tell(Tone.SUCCESS, "Order SO-10041 saved");
                showcase.reportPaymentFailure();
            } else {
                showcase.getWorkbench().getSearchAction().perform();
                ((TextField) scene.lookup(".action-search-field")).setText("order");
            }
            WritableImage image = render(scene);

            assertThat(image.getWidth()).isEqualTo(Showcase.WIDTH * SCALE);
            assertThat(image.getHeight()).isEqualTo(Showcase.HEIGHT * SCALE);
            assertThat(scene.getRoot().lookupAll(".stripe-button")).hasSize(5);
            assertThat(scene.getRoot().lookupAll(".tool-window").stream().filter(Node::isVisible)).hasSize(3);
            assertThat(texts(scene, ".content-header")).contains("SO-10042");
            if (mode == ThemeMode.DARK) {
                assertThat(texts(scene, ".notification-title"))
                        .containsExactlyInAnyOrder("Order SO-10041 saved", "Could not reach the payment service");
            } else {
                assertThat(texts(scene, ".action-search-list")).contains("New Order", "Delete Order");
            }
            save(image, "workbench", mode);
        } finally {
            stage.hide();
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ThemeMode.class, names = {"LIGHT", "DARK"})
    @DisplayName("Shows the controls of JavaFX and of the library in the gallery")
    void showsGallery(ThemeMode mode) throws IOException {
        Scene scene = new Scene(ControlGallery.create(), Showcase.WIDTH, ControlGallery.HEIGHT);
        themes(mode).apply(scene);
        WritableImage image = render(scene);

        assertThat(image.getWidth()).isEqualTo(Showcase.WIDTH * SCALE);
        assertThat(image.getHeight()).isEqualTo(ControlGallery.HEIGHT * SCALE);
        assertThat(descendants(scene.getRoot()).map(node -> node.getClass().getSimpleName()))
                .contains("Button", "ToggleButton", "MenuButton", "SplitMenuButton", "Hyperlink", "CheckBox",
                        "RadioButton", "TextField", "PasswordField", "SearchField", "TextArea", "ComboBox",
                        "ChoiceBox", "Spinner", "DatePicker", "ColorPicker", "ProgressBar", "ProgressIndicator",
                        "Slider", "ListView", "TreeView", "TableView", "Badge", "IconView", "Accordion",
                        "TitledPane", "TabPane", "Pagination", "EmptyState");
        save(image, "controls", mode);
    }
}
