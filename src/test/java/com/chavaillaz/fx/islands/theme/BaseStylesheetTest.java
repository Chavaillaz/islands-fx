package com.chavaillaz.fx.islands.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javafx.application.ColorScheme;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.TraversalDirection;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.control.Tone;
import com.chavaillaz.fx.islands.control.ToneRows;
import com.chavaillaz.fx.islands.control.Validation;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.workbench.TaskStatus;
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

    @Test
    @DisplayName("Pops the calendar of a date picker up as a menu, the day chosen in the accent")
    void popsCalendarUp() {
        DatePicker picker = new DatePicker(LocalDate.of(2026, 11, 26));
        DatePickerSkin skin = new DatePickerSkin(picker);
        picker.setSkin(skin);
        Region calendar = (Region) skin.getPopupContent();
        Scene scene = themed(ThemeMode.DARK, calendar);

        assertThat(fills(calendar)).first().isEqualTo(color(scene, "-popup-border"));
        assertThat(descendants(calendar).filter(node -> node.getStyleClass().contains("selected")))
                .singleElement()
                .satisfies(day -> assertThat(fills((Region) day)).containsExactly(color(scene, "-accent")));
    }

    @Test
    @DisplayName("Marks the page shown among the flat buttons of the pages")
    void marksPageShown() {
        Pagination pagination = new Pagination(5, 1);
        Scene scene = themed(ThemeMode.LIGHT, pagination);
        List<ToggleButton> pages = descendants(pagination)
                .filter(node -> node.getStyleClass().contains("number-button"))
                .map(ToggleButton.class::cast)
                .toList();

        assertThat(pages).hasSize(5);
        assertThat(fills(pages.get(1))).first().isEqualTo(color(scene, "-tab-selected-border"));
        assertThat(fills(pages.get(0))).containsOnly(Color.TRANSPARENT);
    }

    @Test
    @DisplayName("Rings the focus of a button when it comes from the keyboard, not from a click")
    void ringsKeyboardFocus() {
        Button first = new Button("First");
        Button second = new Button("Second");
        Scene scene = themed(ThemeMode.DARK, first, second);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        try {
            first.requestFocus();
            scene.getRoot().applyCss();
            assertThat(first.isFocused()).isTrue();
            assertThat(fills(first)).first().isEqualTo(color(scene, "-control-border"));

            first.requestFocusTraversal(TraversalDirection.NEXT);
            scene.getRoot().applyCss();
            assertThat(second.isFocusVisible()).isTrue();
            assertThat(fills(second)).first().isEqualTo(color(scene, "-accent"));
        } finally {
            stage.hide();
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ThemeMode.class, names = {"LIGHT", "DARK"})
    @DisplayName("Outlines an input failing validation in the color of the problem, stronger while focused")
    void outlinesInvalidInputs(ThemeMode mode) {
        TextField wrong = new TextField();
        Validation.setError(wrong, "Wrong");
        ComboBox<String> doubtful = new ComboBox<>();
        Validation.setWarning(doubtful, "Doubtful");
        Scene scene = themed(mode, wrong, doubtful);

        assertThat(fills(wrong)).first().isEqualTo(color(scene, "-control-error-border-inactive"));
        assertThat(fills(doubtful)).first().isEqualTo(color(scene, "-control-warning-border-inactive"));

        wrong.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
        wrong.applyCss();
        assertThat(fills(wrong)).first().isEqualTo(color(scene, "-control-error-border"));
    }

    @Test
    @DisplayName("Tints the rows of a table after the tone of their item, under the row hovered and the selection")
    void tintsRows() {
        TableView<String> table = new TableView<>(FXCollections.observableArrayList("late", "paid"));
        table.getColumns().add(new TableColumn<>("Order"));
        table.setRowFactory(ToneRows.forTable(item -> item.equals("late") ? Tone.DANGER : null));
        Scene scene = themed(ThemeMode.DARK, table);
        // The rows took their tone in the last layout, after the styles were applied
        table.applyCss();
        List<Region> rows = descendants(table)
                .filter(node -> node.getStyleClass().contains("table-row-cell"))
                .map(Region.class::cast)
                .filter(row -> !((TableRow<?>) row).isEmpty() && row.isVisible())
                .sorted(Comparator.comparingInt(row -> ((TableRow<?>) row).getIndex()))
                .toList();

        assertThat(rows).hasSize(2);
        assertThat(fills(rows.get(0))).containsExactly(color(scene, "-badge-danger-bg"));
        assertThat(fills(rows.get(1))).containsExactly(color(scene, "-island-bg"));

        rows.get(0).pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
        rows.get(0).applyCss();
        assertThat(fills(rows.get(0)))
                .containsExactly(color(scene, "-badge-danger-bg"), color(scene, "-selection-hovered"));

        table.getSelectionModel().select(0);
        rows.get(0).applyCss();
        assertThat(fills(rows.get(0))).containsExactly(color(scene, "-selection-inactive"));
    }

    @Test
    @DisplayName("Tells whether the button of a toggle action is on")
    void showsToggleActionSelected() {
        SimpleBooleanProperty on = new SimpleBooleanProperty(true);
        ButtonBase button = ActionButtons.create(Action.builder("test.filter", "Filter").icon(Icons.FILTER)
                .selected(on).handler(() -> on.set(!on.get())).build());
        Scene scene = themed(ThemeMode.DARK, button);
        assertThat(fills((Region) button)).containsExactly(color(scene, "-toolbar-pressed"));

        on.set(false);
        button.applyCss();
        assertThat(fills((Region) button)).containsOnly(Color.TRANSPARENT);
    }

    @Test
    @DisplayName("Colors and sizes the labels and texts given a text class, as the texts of the themes")
    void stylesTextClasses() {
        Label muted = new Label("Muted");
        muted.getStyleClass().addAll("text-muted", "text-small");
        Text error = new Text("Error");
        error.getStyleClass().addAll("text-error", "text-bold");
        Label code = new Label("SO-1");
        code.getStyleClass().add("monospace");
        Scene scene = themed(ThemeMode.LIGHT, muted, error, code);

        assertThat(muted.getTextFill()).isEqualTo(color(scene, "-text-muted"));
        assertThat(muted.getFont().getSize()).isEqualTo(11);
        assertThat(error.getFill()).isEqualTo(color(scene, "-text-error"));
        assertThat(error.getFont().getStyle()).containsIgnoringCase("bold");
        assertThat(code.getFont().getFamily()).isEqualTo(ThemeManager.MONOSPACED_FONTS.stream()
                .filter(Font.getFamilies()::contains)
                .findFirst()
                .orElse(Font.font("monospace", 13).getFamily()));
    }

    @Test
    @DisplayName("Draws how far a task went in its status, the percentage leaving the room to the pie")
    void drawsTaskProgress() {
        TaskStatus status = new TaskStatus();
        status.running("Synchronizing", 0.6);
        Scene scene = themed(ThemeMode.LIGHT, status);
        Bounds pie = status.getGraphic().localToScene(status.getGraphic().getLayoutBounds());
        PixelReader pixels = scene.getRoot().snapshot(null, null).getPixelReader();
        int background = pixels.getArgb((int) scene.getWidth() - 1, (int) scene.getHeight() - 1);

        long drawn = IntStream.range((int) pie.getMinY(), (int) pie.getMaxY())
                .mapToLong(y -> IntStream.range((int) pie.getMinX(), (int) pie.getMaxX())
                        .filter(x -> pixels.getArgb(x, y) != background)
                        .count())
                .sum();
        assertThat(drawn).isGreaterThan(Math.round(pie.getWidth() * pie.getHeight() / 3));
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
