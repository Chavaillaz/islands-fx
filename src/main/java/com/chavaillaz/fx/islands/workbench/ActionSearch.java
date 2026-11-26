package com.chavaillaz.fx.islands.workbench;

import static java.util.Objects.requireNonNullElse;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.internal.Texts;

/**
 * The search of the actions of the workbench: a field popping up over the window, under the main toolbar, listing the
 * actions whose text, menu or description holds the words typed, the best matches first, and performing the one
 * chosen with the keyboard or the mouse.
 * <p>
 * It is a layer over the workbench, covering the window while open: a click beside the popup closes it, as the escape
 * key does, the focus going back where it was. The popup carries the style class {@code action-search}, its field
 * {@code action-search-field} and its list {@code action-search-list}, whose cells show the icon, the text, the menu or
 * the description ({@code action-search-secondary}), and the shortcut ({@code action-search-shortcut}) of an action.
 */
final class ActionSearch extends StackPane {

    /** The width of the popup, in pixels. */
    static final double WIDTH = 600;

    /** The height of the list of the actions found, in pixels. */
    static final double LIST_HEIGHT = 320;

    private static final Pattern MARKS = Pattern.compile("\\p{M}");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private final Supplier<List<Entry>> entries;
    private final TextField field = new TextField();
    private final ListView<Entry> list = new ListView<>();
    private @Nullable Node focusBefore;

    /**
     * Creates the search, closed.
     *
     * @param entries gives the actions to search, each time the search opens
     */
    ActionSearch(Supplier<List<Entry>> entries) {
        this.entries = entries;
        getStyleClass().add("action-search-layer");
        setVisible(false);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(MainToolbar.HEIGHT + 8, 16, 16, 16));

        field.getStyleClass().add("action-search-field");
        field.setPromptText(Texts.get("search.prompt"));
        field.textProperty().addListener((observable, previous, text) -> {
            if (isVisible()) {
                show(text);
            }
        });
        field.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);

        list.getStyleClass().addAll("action-search-list", "edge-to-edge");
        list.setPlaceholder(new Label(Texts.get("search.none")));
        list.setCellFactory(view -> new EntryCell());
        list.setPrefHeight(LIST_HEIGHT);
        list.setFocusTraversable(false);

        VBox popup = new VBox(field, list);
        popup.getStyleClass().add("action-search");
        popup.setPrefWidth(WIDTH);
        popup.setMaxWidth(WIDTH);
        popup.setMaxHeight(Region.USE_PREF_SIZE);
        getChildren().add(popup);
        setOnMousePressed(event -> {
            if (event.getTarget() == this) {
                close();
            }
        });
    }

    /**
     * Opens the search, listing every action, the focus in its field; or selects what was typed when open already.
     */
    void open() {
        if (isVisible()) {
            field.selectAll();
            field.requestFocus();
            return;
        }
        Scene scene = getScene();
        focusBefore = scene == null ? null : scene.getFocusOwner();
        field.clear();
        show("");
        setVisible(true);
        field.requestFocus();
    }

    /**
     * Closes the search, the focus going back where it was before it opened.
     */
    void close() {
        setVisible(false);
        Node previous = focusBefore;
        focusBefore = null;
        if (previous != null && previous.getScene() == getScene()) {
            previous.requestFocus();
        }
    }

    /**
     * Tells whether the search is open.
     *
     * @return {@code true} while open
     */
    boolean isOpen() {
        return isVisible();
    }

    /**
     * Gives the field the user types in, for the tests to type.
     */
    TextField getField() {
        return field;
    }

    /**
     * Gives the list of the actions found, for the tests to look into.
     */
    ListView<Entry> getList() {
        return list;
    }

    /**
     * Gives the entries holding every word of a query in the text, menu or description of their action, those whose
     * text starts with the query first, then those with a word of their text starting with its first word, then those
     * whose text holds it, the others last. Neither the case nor the accents matter.
     *
     * @param entries the entries
     * @param query   the words typed
     * @return the entries found, every entry for a blank query
     */
    static List<Entry> filter(List<Entry> entries, String query) {
        String normalized = normalize(query.strip());
        if (normalized.isEmpty()) {
            return List.copyOf(entries);
        }
        String[] words = SPACES.split(normalized);
        record Scored(Entry entry, int score) {
        }
        return entries.stream()
                .map(entry -> new Scored(entry, score(entry, normalized, words)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt((Scored scored) -> scored.score()).reversed())
                .map(Scored::entry)
                .toList();
    }

    private static int score(Entry entry, String query, String[] words) {
        String text = normalize(entry.action().getText());
        String other = normalize(entry.path() + ' ' + requireNonNullElse(entry.action().getDescription(), ""));
        for (String word : words) {
            if (!text.contains(word) && !other.contains(word)) {
                return 0;
            }
        }
        if (text.startsWith(query)) {
            return 4;
        } else if (Arrays.stream(SPACES.split(text)).anyMatch(part -> part.startsWith(words[0]))) {
            return 3;
        }
        return text.contains(query) ? 2 : 1;
    }

    private static String normalize(String text) {
        return MARKS.matcher(Normalizer.normalize(text, Normalizer.Form.NFD)).replaceAll("").toLowerCase(Locale.ROOT);
    }

    private void show(String query) {
        list.getItems().setAll(filter(entries.get(), query));
        if (!list.getItems().isEmpty()) {
            list.getSelectionModel().selectFirst();
            list.scrollTo(0);
        }
    }

    private void onKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case UP -> move(-1);
            case DOWN -> move(1);
            case ENTER -> perform();
            case ESCAPE -> close();
            default -> {
                return;
            }
        }
        event.consume();
    }

    private void move(int offset) {
        int size = list.getItems().size();
        if (size > 0) {
            int index = Math.clamp(list.getSelectionModel().getSelectedIndex() + (long) offset, 0, size - 1);
            list.getSelectionModel().select(index);
            list.scrollTo(index);
        }
    }

    private void perform() {
        Entry entry = list.getSelectionModel().getSelectedItem();
        if (entry != null && !entry.action().isDisabled()) {
            close();
            entry.action().perform();
        }
    }

    /**
     * An action the search finds, and where it stands.
     *
     * @param action the action
     * @param path   the menus holding the action, as {@code View › Theme}, or empty for an action out of the main menu
     */
    record Entry(Action action, String path) {
    }

    /**
     * A cell showing an action: its icon, or a tick for a toggle action selected, its text, its menus or its
     * description, and its shortcut. It is disabled along with the action, and performs it when clicked.
     */
    private final class EntryCell extends ListCell<Entry> {

        private final IconView icon = new IconView();
        private final Label text = new Label();
        private final Label secondary = new Label();
        private final Label shortcut = new Label();
        private final HBox box;

        private EntryCell() {
            getStyleClass().add("action-search-cell");
            text.setMnemonicParsing(false);
            text.setMinWidth(Region.USE_PREF_SIZE);
            secondary.getStyleClass().add("action-search-secondary");
            secondary.setMnemonicParsing(false);
            shortcut.getStyleClass().add("action-search-shortcut");
            shortcut.setMinWidth(Region.USE_PREF_SIZE);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            box = new HBox(8, icon, text, secondary, spacer, shortcut);
            box.setAlignment(Pos.CENTER_LEFT);
            setOnMouseClicked(event -> {
                if (!isEmpty()) {
                    perform();
                }
            });
        }

        @Override
        protected void updateItem(@Nullable Entry entry, boolean empty) {
            super.updateItem(entry, empty);
            setText(null);
            if (empty || entry == null) {
                setGraphic(null);
                setDisable(false);
                return;
            }
            Action action = entry.action();
            icon.setIcon(action.isToggle() && action.isSelected() ? Icons.CHECK : action.getIcon());
            text.setText(action.getText());
            secondary.setText(entry.path().isEmpty() ? requireNonNullElse(action.getDescription(), "") : entry.path());
            KeyCombination accelerator = action.getAccelerator();
            shortcut.setText(accelerator == null ? "" : accelerator.getDisplayText());
            setDisable(action.isDisabled());
            setGraphic(box);
        }
    }
}
