package com.chavaillaz.fx.islands.showcase;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.control.EmptyState;
import com.chavaillaz.fx.islands.control.SearchField;
import com.chavaillaz.fx.islands.control.Tone;
import com.chavaillaz.fx.islands.control.ToneRows;
import com.chavaillaz.fx.islands.control.Validation;
import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.workbench.Island;

/**
 * The gallery of the controls of JavaFX and of those of the library, each in its usual states, such as selected,
 * disabled or failing its validation, in the theme of its window.
 */
final class ControlGallery {

    /** The height of the window, in pixels, showing every section. */
    static final double HEIGHT = 892;

    /** The number of columns of sections. */
    static final int COLUMNS = 4;

    private ControlGallery() {
    }

    /**
     * Creates the content of the window of the gallery: an island holding the sections of the controls, which scrolls
     * once the window is too small to show them all.
     *
     * @return the content of the window
     */
    static Region create() {
        GridPane grid = new GridPane(28, 24);
        grid.setPadding(new Insets(18, 24, 24, 24));
        grid.add(section("Buttons", buttons()), 0, 0);
        grid.add(section("Choices", choices()), 1, 0);
        grid.add(section("Text inputs", textInputs()), 2, 0);
        grid.add(section("Pickers", pickers()), 3, 0);
        grid.add(section("Validation", validation()), 0, 1);
        grid.add(section("Progress", progress()), 1, 1);
        grid.add(section("List", list()), 2, 1);
        grid.add(section("Tree", tree()), 3, 1);
        grid.add(section("Table", table()), 0, 2, 2, 1);
        grid.add(section("Badges and texts", badgesAndTexts()), 2, 2);
        grid.add(section("Icons", icons()), 3, 2);
        grid.add(section("Titled panes", titledPanes()), 0, 3);
        grid.add(section("Tabs", tabs()), 1, 3);
        grid.add(section("Pagination", pagination()), 2, 3);
        grid.add(section("Empty state", emptyState()), 3, 3);
        for (int index = 0; index < COLUMNS; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / COLUMNS);
            grid.getColumnConstraints().add(column);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        StackPane content = new StackPane(new Island(scroll));
        content.setPadding(new Insets(6));
        return content;
    }

    private static VBox section(String title, Node content) {
        Label label = new Label(title);
        label.getStyleClass().add("text-bold");
        return new VBox(10, label, content);
    }

    private static Node buttons() {
        Button save = new Button("Save");
        save.setDefaultButton(true);
        Button disabled = new Button("Disabled");
        disabled.setDisable(true);
        ToggleButton bold = new ToggleButton("Bold");
        bold.setSelected(true);
        MenuButton export = new MenuButton("Export", null, new MenuItem("As CSV"), new MenuItem("As PDF"));
        SplitMenuButton run = new SplitMenuButton(new MenuItem("Run Again"));
        run.setText("Run");

        SimpleBooleanProperty filtered = new SimpleBooleanProperty(true);
        Action filter = Action.builder("gallery.filter", "Filter").icon(Icons.FILTER).selected(filtered)
                .handler(() -> filtered.set(!filtered.get())).build();
        HBox actions = new HBox(2, ActionButtons.create(action("Add", Icons.ADD)),
                ActionButtons.create(action("Edit", Icons.EDIT)), ActionButtons.create(action("Copy", Icons.COPY)),
                ActionButtons.create(action("Delete", Icons.DELETE)), ActionButtons.create(filter));
        return new VBox(10, new HBox(8, save, new Button("Cancel"), disabled),
                new HBox(8, bold, new ToggleButton("Italic"), export, run),
                actions, new Hyperlink("Read the documentation"));
    }

    private static Node choices() {
        CheckBox paid = new CheckBox("Paid");
        paid.setSelected(true);
        CheckBox refunded = new CheckBox("Partly refunded");
        refunded.setIndeterminate(true);
        CheckBox archived = new CheckBox("Archived");
        archived.setSelected(true);
        archived.setDisable(true);

        ToggleGroup payment = new ToggleGroup();
        RadioButton card = new RadioButton("Card");
        RadioButton invoice = new RadioButton("Invoice");
        RadioButton cash = new RadioButton("Cash");
        cash.setDisable(true);
        for (RadioButton button : List.of(card, invoice, cash)) {
            button.setToggleGroup(payment);
        }
        card.setSelected(true);
        return new HBox(32, new VBox(10, paid, new CheckBox("Shipped"), refunded, archived),
                new VBox(10, card, invoice, cash));
    }

    private static Node textInputs() {
        TextField email = new TextField();
        email.setPromptText("Email address");
        PasswordField password = new PasswordField();
        password.setText("correct horse");
        SearchField search = new SearchField();
        search.setText("keyboard");
        TextArea note = new TextArea("Leave the parcel at the reception,\nwhich opens at 8 o'clock.");
        note.setPrefRowCount(2);
        return new VBox(8, new TextField("Ada Lovelace"), email, password, search, note);
    }

    private static Node pickers() {
        ComboBox<String> currency = new ComboBox<>(FXCollections.observableArrayList("CHF", "EUR", "USD"));
        currency.setValue("CHF");
        ComboBox<String> city = new ComboBox<>(FXCollections.observableArrayList("Geneva", "Lausanne", "Zurich"));
        city.setEditable(true);
        city.setValue("Geneva");
        ChoiceBox<String> delivery = new ChoiceBox<>(
                FXCollections.observableArrayList("Standard delivery", "Express delivery"));
        delivery.setValue("Express delivery");
        Spinner<Integer> quantity = new Spinner<>(1, 99, 3);
        DatePicker date = new DatePicker(LocalDate.of(2026, 11, 25));
        ColorPicker color = new ColorPicker(Color.web("#3871E1"));
        List<Region> pickers = List.of(currency, city, delivery, quantity, date, color);
        for (Region picker : pickers) {
            picker.setMaxWidth(Double.MAX_VALUE);
        }
        VBox box = new VBox(8);
        box.getChildren().setAll(pickers);
        return box;
    }

    private static Node validation() {
        TextField name = new TextField();
        name.setPromptText("Name of the customer");
        Validation.setError(name, "The name is required");
        Label error = new Label("The name is required");
        error.getStyleClass().addAll("text-error", "text-small");

        ComboBox<String> currency = new ComboBox<>(FXCollections.observableArrayList("CHF", "EUR"));
        currency.setValue("EUR");
        currency.setMaxWidth(Double.MAX_VALUE);
        Validation.setWarning(currency, "The shop invoices in CHF");
        Label warning = new Label("The shop invoices in CHF");
        warning.getStyleClass().addAll("text-warning", "text-small");
        return new VBox(6, name, error, currency, warning);
    }

    private static Node progress() {
        ProgressBar bar = new ProgressBar(0.65);
        bar.setMaxWidth(Double.MAX_VALUE);
        Slider slider = new Slider(0, 100, 40);
        Slider ticks = new Slider(0, 100, 75);
        ticks.setShowTickMarks(true);
        ticks.setMajorTickUnit(25);
        ticks.setMinorTickCount(0);
        return new VBox(14, bar, new HBox(16, new ProgressIndicator(0.4), new ProgressIndicator(1)), slider, ticks);
    }

    private static Node list() {
        ListView<String> list = new ListView<>(FXCollections.observableArrayList("Ada Lovelace", "Alan Turing",
                "Grace Hopper", "Katherine Johnson", "Margaret Hamilton"));
        list.getSelectionModel().select(1);
        list.setPrefHeight(160);
        return list;
    }

    private static Node tree() {
        TreeItem<String> november = folder("November 2026", List.of(order("SO-10042"), order("SO-10043")));
        TreeItem<String> root = folder("Orders", List.of(november, folder("October 2026", List.of(order("SO-10031")))));
        TreeView<String> tree = new TreeView<>(root);
        tree.getSelectionModel().select(november.getChildren().getFirst());
        tree.setPrefHeight(160);
        return tree;
    }

    private static TreeItem<String> folder(String name, List<TreeItem<String>> children) {
        TreeItem<String> folder = new TreeItem<>(name, new IconView(Icons.FOLDER));
        folder.getChildren().setAll(children);
        folder.setExpanded(true);
        return folder;
    }

    private static TreeItem<String> order(String number) {
        return new TreeItem<>(number, new IconView(Icons.RECORD));
    }

    private static Node table() {
        TableView<Order> table = new TableView<>(FXCollections.observableArrayList(Order.samples().subList(9, 14)));
        table.getColumns().setAll(Order.columns());
        table.setRowFactory(ToneRows.forTable(order -> switch (order.status()) {
            case LATE -> Tone.DANGER;
            case PENDING -> Tone.WARNING;
            default -> null;
        }));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().select(2);
        table.setPrefHeight(178);
        return table;
    }

    private static Node badgesAndTexts() {
        FlowPane badges = new FlowPane(6, 6);
        for (Order.Status status : Order.Status.values()) {
            badges.getChildren().add(status.badge());
        }
        VBox texts = new VBox(4, text("Default text"), text("Muted text", "text-muted"),
                text("Secondary text", "text-secondary"), text("Error text", "text-error"),
                text("Warning text", "text-warning"));
        VBox styles = new VBox(4, text("Success text", "text-success"), text("Bold text", "text-bold"),
                text("Small text", "text-small"), text("Large text", "text-large"), text("Monospace", "monospace"));
        return new VBox(12, badges, new HBox(24, texts, styles));
    }

    private static Label text(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private static Node icons() {
        FlowPane icons = new FlowPane(14, 14);
        for (Icons icon : Icons.values()) {
            IconView view = new IconView(icon);
            Tooltip.install(view, new Tooltip(icon.name()));
            icons.getChildren().add(view);
        }
        icons.setPrefWrapLength(240);
        return icons;
    }

    private static Node titledPanes() {
        TitledPane delivery = new TitledPane("Delivery", new Label("Express, before noon"));
        Accordion accordion = new Accordion(delivery, new TitledPane("Payment", new Label("Card ending in 1234")),
                new TitledPane("Notes", new Label("None")));
        accordion.setExpandedPane(delivery);
        return accordion;
    }

    private static Node tabs() {
        Tab details = new Tab("Details", new Label("Placed on 20 November 2026"));
        details.setClosable(false);
        TabPane tabs = new TabPane(details, new Tab("Lines"), new Tab("History"));
        tabs.setPrefHeight(110);
        return tabs;
    }

    private static Node pagination() {
        Pagination pagination = new Pagination(12, 2);
        pagination.setMaxPageIndicatorCount(5);
        pagination.setPageFactory(page -> new Label("Page " + (page + 1) + " of 12"));
        return pagination;
    }

    private static Node emptyState() {
        EmptyState empty = new EmptyState();
        empty.setTitle("No order yet");
        empty.setText("The orders placed online show here");
        empty.getActions().setAll(Action.builder("gallery.new-order", "New Order").icon(Icons.ADD)
                .accelerator(KeyCombination.keyCombination("Shortcut+N")).handler(() -> { }).build());
        return empty;
    }

    private static Action action(String text, Icon icon) {
        return Action.builder("gallery." + text.toLowerCase(Locale.ROOT).replace(' ', '-'), text)
                .icon(icon).handler(() -> { }).build();
    }
}
