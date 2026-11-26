package com.chavaillaz.fx.islands.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.application.ColorScheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.workbench.ToolWindow;
import com.chavaillaz.fx.islands.workbench.Workbench;

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

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ListView.class, TreeView.class, TableView.class, TreeTableView.class})
    @DisplayName("Frames the lists, trees and tables standing among other controls")
    void framesViews(Class<?> type) throws ReflectiveOperationException {
        Control view = (Control) type.getConstructor().newInstance();
        Scene scene = themed(ThemeMode.DARK, new TextField(), view);

        assertThat(fills(view)).first().isEqualTo(color(scene, "-control-border"));
    }

    @Test
    @DisplayName("Leaves unframed the lists, trees and tables filling an island, which frames them")
    void leavesViewsFillingIslandsUnframed() {
        TableView<String> content = new TableView<>();
        ListView<String> toolWindowContent = new ListView<>();
        TreeView<String> deeper = new TreeView<>();
        deeper.getStyleClass().add("edge-to-edge");
        Workbench workbench = new Workbench();
        workbench.setContent(content);
        workbench.addToolWindow(ToolWindow.builder("list", "List", Icons.LIST, toolWindowContent).build());
        workbench.addToolWindow(ToolWindow.builder("tree", "Tree", Icons.FOLDER, new VBox(new TextField(), deeper))
                .build());
        themed(ThemeMode.DARK, workbench.getNode());

        assertThat(List.of(content, toolWindowContent, deeper))
                .allSatisfy(view -> assertThat(fills(view)).containsOnly(Color.TRANSPARENT));
    }

    /**
     * Shows nodes in a scene of a theme, styled and laid out twice, for the nodes skins add while laid out.
     */
    private Scene themed(ThemeMode mode, Node... nodes) {
        VBox root = new VBox(nodes);
        Scene scene = new Scene(root, 600, 400);
        new ThemeManager(mode, system).apply(scene);
        for (int pass = 0; pass < 2; pass++) {
            root.applyCss();
            root.layout();
        }
        return scene;
    }

    /**
     * Gives the colors a region is painted with, from the back to the front.
     */
    static List<Paint> fills(Region region) {
        return region.getBackground() == null
                ? List.of()
                : region.getBackground().getFills().stream().map(BackgroundFill::getFill).toList();
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
