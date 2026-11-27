package com.chavaillaz.fx.islands.showcase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.action.ActionGroup;
import com.chavaillaz.fx.islands.action.ActionMenus;
import com.chavaillaz.fx.islands.control.EmptyState;
import com.chavaillaz.fx.islands.control.SearchField;
import com.chavaillaz.fx.islands.control.Tone;
import com.chavaillaz.fx.islands.control.ToneRows;
import com.chavaillaz.fx.islands.control.Validation;
import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.theme.ThemeManager;
import com.chavaillaz.fx.islands.theme.ThemeMode;
import com.chavaillaz.fx.islands.workbench.ContentTabs;
import com.chavaillaz.fx.islands.workbench.Notification;
import com.chavaillaz.fx.islands.workbench.TaskStatus;
import com.chavaillaz.fx.islands.workbench.TitleWidget;
import com.chavaillaz.fx.islands.workbench.ToolWindow;
import com.chavaillaz.fx.islands.workbench.ToolWindowAnchor;
import com.chavaillaz.fx.islands.workbench.Workbench;

/**
 * The showcase of the library: the main window of an application taking orders, which a workbench lays out, and the
 * gallery of the controls in the theme the user chooses.
 * <p>
 * {@code mvn -Pshowcase} opens it in the theme of the operating system, and {@code ShowcaseTest} renders it to the
 * screenshots of the README. Its orders and their customers are made up.
 */
public final class Showcase {

    /** The width of the windows, in pixels. */
    static final double WIDTH = 1280;

    /** The height of the windows, in pixels. */
    static final double HEIGHT = 800;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DAY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final Workbench workbench;
    private final ThemeManager themes;
    private final TaskStatus status = new TaskStatus();
    private final TableView<Order> orderTable = new TableView<>();
    private final ContentTabs orderTabs = new ContentTabs();
    private final OrderForm form = new OrderForm();
    private final ToolWindow log;
    private final Action retry;
    private final Action details;

    /**
     * Lays the main window out in a workbench, the orders SO-10040, SO-10042 and SO-10043 open, the second selected.
     *
     * @param workbench the workbench of the main window
     * @param themes    the manager of the themes, which the menus offer to change
     */
    public Showcase(Workbench workbench, ThemeManager themes) {
        this.workbench = workbench;
        this.themes = themes;
        List<Order> orders = Order.samples();

        Action newOrder = action("orders.new", "New Order", Icons.ADD, "Shortcut+N",
                () -> tell(Tone.INFO, "Order SO-10046 created"));
        Action refresh = action("orders.refresh", "Refresh Orders", Icons.REFRESH, "Shortcut+R", this::refresh);
        Action export = action("orders.export", "Export Orders", Icons.EXPORT, null,
                () -> tell(Tone.SUCCESS, "Orders exported to orders.csv"));
        Action edit = action("order.edit", "Edit Order", Icons.EDIT, "Shortcut+E", () -> { });
        Action copy = action("order.copy", "Copy Order Number", Icons.COPY, null, () -> { });
        Action delete = action("order.delete", "Delete Order", Icons.DELETE, null, () -> selectedOrder()
                .ifPresent(order -> tell(Tone.WARNING, "Order " + order.number() + " deleted")));
        Action close = action("order.close", "Close Tab", Icons.CLOSE, "Shortcut+W", this::closeTab);
        Action gallery = action("showcase.gallery", "Control Gallery", Icons.BOOK, "Shortcut+G", this::showGallery);
        retry = action("payment.retry", "Retry", Icons.REFRESH, null, this::refresh);
        details = action("payment.details", "Show the Log", Icons.LIST, null, () -> getLog().setVisible(true));

        ToolWindow ordersWindow = ToolWindow.builder("orders", "Orders", Icons.RECORDS, orders(orders))
                .accelerator(KeyCombination.keyCombination("Shortcut+1"))
                .width(420)
                .actions(newOrder, refresh)
                .build();
        ToolWindow customers = ToolWindow.builder("customers", "Customers", Icons.FOLDER, customers(orders))
                .accelerator(KeyCombination.keyCombination("Shortcut+2"))
                .build();
        ToolWindow properties = ToolWindow.builder("properties", "Properties", Icons.PROPERTY, form.getNode())
                .anchor(ToolWindowAnchor.RIGHT)
                .accelerator(KeyCombination.keyCombination("Shortcut+3"))
                .width(290)
                .build();
        log = ToolWindow.builder("log", "Log", Icons.LIST, log())
                .anchor(ToolWindowAnchor.BOTTOM)
                .accelerator(KeyCombination.keyCombination("Shortcut+4"))
                .height(150)
                .build();
        ToolWindow problems = ToolWindow.builder("problems", "Problems", Icons.WARNING, problems(refresh))
                .anchor(ToolWindowAnchor.BOTTOM)
                .accelerator(KeyCombination.keyCombination("Shortcut+5"))
                .build();
        for (ToolWindow toolWindow : List.of(ordersWindow, customers, properties, log, problems)) {
            workbench.addToolWindow(toolWindow);
        }
        customers.setVisible(false);
        problems.setVisible(false);
        log.setVisible(true);

        EmptyState noOrder = new EmptyState();
        noOrder.setTitle("No order open");
        noOrder.setText("Choose an order in the list to open it here");
        noOrder.getActions().setAll(ordersWindow.getToggleAction());
        noOrder.visibleProperty().bind(Bindings.isEmpty(orderTabs.getTabs()));
        orderTabs.visibleProperty().bind(Bindings.isNotEmpty(orderTabs.getTabs()));
        orderTabs.getActions().setAll(edit, copy, delete);
        orderTabs.getSelectionModel().selectedItemProperty().subscribe(this::showSelected);
        workbench.setContent(new StackPane(noOrder, orderTabs));
        for (String number : List.of("SO-10040", "SO-10043", "SO-10042")) {
            orderTable.getSelectionModel().select(find(orders, number));
        }
        orderTabs.getTabs().sort((first, second) -> first.getText().compareTo(second.getText()));

        workbench.getMainToolbar().getLeftItems().addAll(List.of(new TitleWidget("Order Desk"),
                ActionButtons.create(newOrder), ActionButtons.create(refresh), ActionButtons.create(export)));
        workbench.getMainToolbar().getRightItems().addAll(List.of(ActionButtons.create(workbench.getSearchAction()),
                ActionButtons.create(gallery)));
        workbench.getStatusBar().getWidgets().setAll(List.of(status, new Label(orders.size() + " orders")));
        workbench.setMainMenu(ActionMenus.createMenus(List.of(
                ActionGroup.of("File", newOrder, refresh, export),
                ActionGroup.of("Order", edit, copy, delete, close),
                ActionGroup.of("View", ordersWindow.getToggleAction(), customers.getToggleAction(),
                        properties.getToggleAction(), log.getToggleAction(), problems.getToggleAction(),
                        themes.createMenu(), gallery),
                ActionGroup.of("Help", workbench.getSearchAction()))));
    }

    /**
     * Opens the showcase in the theme of the operating system, in a window that the main menu of macOS serves.
     *
     * @param args the arguments, ignored
     */
    public static void main(String[] args) {
        Platform.startup(() -> {
            ThemeManager themes = new ThemeManager(ThemeMode.SYSTEM);
            themes.install();
            Showcase showcase = new Showcase(new Workbench(), themes);
            Stage stage = new Stage();
            stage.setTitle("Order Desk");
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
            showcase.createScene(stage);
            stage.show();
            showcase.refresh();
            showcase.reportPaymentFailure();
        });
    }

    /**
     * Creates the scene of the main window, in the theme chosen, and gives it to the stage before it shows.
     *
     * @param stage the main window
     * @return the scene
     */
    Scene createScene(Stage stage) {
        Scene scene = workbench.createScene(stage);
        themes.apply(scene);
        return scene;
    }

    /**
     * Gives the workbench of the main window.
     *
     * @return the workbench
     */
    Workbench getWorkbench() {
        return workbench;
    }

    /**
     * Gives the status of the synchronization of the orders, in the status bar.
     *
     * @return the status
     */
    TaskStatus getStatus() {
        return status;
    }

    /**
     * Gives the tool window showing the log.
     *
     * @return the tool window
     */
    ToolWindow getLog() {
        return log;
    }

    /**
     * Gives the tabs of the orders open.
     *
     * @return the tabs
     */
    ContentTabs getOrderTabs() {
        return orderTabs;
    }

    /**
     * Notifies that the payment of an order could not be made, until the user closes the notification.
     */
    void reportPaymentFailure() {
        workbench.showNotification(Notification.builder(Tone.DANGER, "Could not reach the payment service")
                .text("The service did not answer within 30 seconds.")
                .actions(retry, details)
                .build());
    }

    /**
     * Notifies something done, which the notification tells until its timeout.
     *
     * @param tone  the tone of the notification
     * @param title the title of the notification
     */
    void tell(Tone tone, String title) {
        workbench.showNotification(Notification.builder(tone, title).build());
    }

    private static Order find(List<Order> orders, String number) {
        return orders.stream().filter(order -> order.number().equals(number)).findFirst().orElseThrow();
    }

    /**
     * Shows an order in its tab, opening it unless open already.
     */
    private void open(Order order) {
        Tab tab = orderTabs.getTabs().stream()
                .filter(each -> each.getUserData() == order)
                .findFirst()
                .orElseGet(() -> {
                    Tab opened = new Tab(order.number(), details(order));
                    opened.setGraphic(new IconView(Icons.RECORD));
                    opened.setUserData(order);
                    orderTabs.getTabs().add(opened);
                    return opened;
                });
        orderTabs.getSelectionModel().select(tab);
    }

    private Optional<Order> selectedOrder() {
        Tab tab = orderTabs.getSelectionModel().getSelectedItem();
        return tab != null && tab.getUserData() instanceof Order order ? Optional.of(order) : Optional.empty();
    }

    /**
     * Follows the order selected among the tabs, in the status bar and the properties.
     */
    private void showSelected(Tab tab) {
        if (tab != null && tab.getUserData() instanceof Order order) {
            workbench.getStatusBar().getPath().setAll("Orders", order.number());
            form.show(order);
        } else {
            workbench.getStatusBar().getPath().setAll("Orders");
        }
    }

    private void closeTab() {
        Tab tab = orderTabs.getSelectionModel().getSelectedItem();
        if (tab != null) {
            orderTabs.close(tab);
        }
    }

    /**
     * Synchronizes the orders in the background, the status bar following how far it went.
     */
    private void refresh() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws InterruptedException {
                updateMessage("Synchronizing the orders…");
                for (int step = 1; step <= 30; step++) {
                    Thread.sleep(100);
                    updateProgress(step, 30);
                }
                return null;
            }
        };
        status.follow(task);
        task.setOnSucceeded(event -> tell(Tone.SUCCESS, "Orders synchronized"));
        Thread thread = new Thread(task, "orders-synchronization");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Opens the gallery of the controls, in a window of its own.
     */
    private void showGallery() {
        Stage stage = new Stage();
        stage.setTitle("Control Gallery");
        Scene scene = new Scene(ControlGallery.create(), WIDTH, ControlGallery.HEIGHT);
        themes.apply(scene);
        stage.setScene(scene);
        stage.show();
    }

    private static Action action(String id, String text, Icon icon, String accelerator, Runnable handler) {
        Action.Builder builder = Action.builder(id, text).icon(icon).handler(handler);
        if (accelerator != null) {
            builder.accelerator(KeyCombination.keyCombination(accelerator));
        }
        return builder.build();
    }

    /**
     * Creates the content of the tool window of the orders: a field searching them, above their table, whose order
     * selected opens in its tab.
     */
    private Node orders(List<Order> orders) {
        SearchField search = new SearchField();
        search.setPromptText("Search orders");
        FilteredList<Order> found = new FilteredList<>(FXCollections.observableArrayList(orders));
        search.textProperty().subscribe(text -> found.setPredicate(order -> order.matches(text)));

        orderTable.setItems(found);
        orderTable.getColumns().setAll(Order.columns());
        orderTable.setRowFactory(ToneRows.forTable(order -> order.status() == Order.Status.LATE ? Tone.DANGER : null));
        orderTable.getStyleClass().add("edge-to-edge");
        orderTable.getSelectionModel().selectedItemProperty().subscribe(order -> {
            if (order != null) {
                open(order);
            }
        });
        VBox.setMargin(search, new Insets(0, 8, 0, 8));
        VBox.setVgrow(orderTable, Priority.ALWAYS);
        return new VBox(6, search, orderTable);
    }

    private static Node customers(List<Order> orders) {
        return new ListView<>(FXCollections.observableArrayList(
                orders.stream().map(Order::customer).distinct().sorted().toList()));
    }

    /**
     * Creates the details of an order: its customer, its status, the lines it orders, its delivery note and history.
     */
    private static Node details(Order order) {
        Label customer = new Label(order.customer());
        customer.getStyleClass().addAll("text-large", "text-bold");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox title = new HBox(10, customer, space, order.status().badge());
        title.setAlignment(Pos.CENTER_LEFT);
        Label placed = new Label("Placed on " + DAY.format(order.placed()) + ", to deliver by "
                + DAY.format(order.placed().plusDays(5)));
        placed.getStyleClass().add("text-secondary");

        TableView<Line> lines = new TableView<>(FXCollections.observableArrayList(lines(order)));
        lines.getColumns().setAll(List.of(
                column("Item", Line::item, 220, false),
                column("Quantity", line -> Integer.toString(line.quantity()), 80, true),
                column("Price", line -> line.price().toPlainString(), 90, true),
                column("Amount", line -> line.amount().toPlainString(), 90, true)));
        lines.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        lines.setPrefHeight(112);
        lines.setMinHeight(Region.USE_PREF_SIZE);

        Label totalText = new Label("Total");
        totalText.getStyleClass().add("text-secondary");
        Label total = new Label(order.amount().toPlainString() + " CHF");
        total.getStyleClass().addAll("text-large", "text-bold");
        HBox totals = new HBox(8, totalText, total);
        totals.setAlignment(Pos.BASELINE_RIGHT);

        TextArea note = new TextArea("Leave the parcel at the reception, which opens at 8 o'clock.");
        note.setWrapText(true);
        note.setPrefRowCount(2);
        note.setMinHeight(Region.USE_PREF_SIZE);
        ListView<String> history = new ListView<>(FXCollections.observableArrayList(history(order)));
        history.setPrefHeight(110);
        VBox.setVgrow(history, Priority.ALWAYS);

        VBox details = new VBox(10, title, placed, lines, totals, label("Delivery note"), note, label("History"),
                history);
        details.setPadding(new Insets(10, 16, 16, 16));
        return details;
    }

    /**
     * Gives the lines of an order: those of SO-10042, or else its items and their shipping, adding up to its amount.
     */
    private static List<Line> lines(Order order) {
        if (order.number().equals("SO-10042")) {
            return List.of(new Line("Mechanical keyboard", 1, new BigDecimal("89.00")),
                    new Line("USB-C cable", 2, new BigDecimal("12.50")),
                    new Line("Desk mat", 1, new BigDecimal("15.90")));
        }
        BigDecimal shipping = new BigDecimal("9.90");
        return order.amount().compareTo(shipping) > 0
                ? List.of(new Line("Items", 1, order.amount().subtract(shipping)), new Line("Shipping", 1, shipping))
                : List.of(new Line("Items", 1, order.amount()));
    }

    /**
     * Gives the history of an order, after where it stands.
     */
    private static List<String> history(Order order) {
        LocalDate placed = order.placed();
        List<String> history = new ArrayList<>();
        history.add(SHORT_DAY.format(placed) + ", 10:12    "
                + (order.status() == Order.Status.DRAFT ? "Draft saved" : "Placed online"));
        switch (order.status()) {
            case PAID -> history.add(SHORT_DAY.format(placed.plusDays(1)) + ", 09:30    Payment received");
            case PENDING -> history.add(SHORT_DAY.format(placed.plusDays(1)) + ", 09:30    Payment to confirm");
            case LATE -> {
                history.add(SHORT_DAY.format(placed.plusDays(3)) + ", 08:00    Payment reminder sent");
                history.add(SHORT_DAY.format(placed.plusDays(5)) + ", 08:00    Payment overdue");
            }
            default -> history.add(SHORT_DAY.format(placed) + ", 10:13    Invoice sent");
        }
        return history;
    }

    private static TableColumn<Line, String> column(String title, Function<Line, String> value, double width,
                                                    boolean alignRight) {
        TableColumn<Line, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        if (alignRight) {
            column.getStyleClass().add("align-right");
        }
        return column;
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("text-secondary");
        return label;
    }

    private static Node log() {
        ListView<String> log = new ListView<>(FXCollections.observableArrayList(
                "10:41:58  INFO   Synchronizing the orders",
                "10:42:01  INFO   Loaded 16 orders in 412 ms",
                "10:42:05  WARN   Order SO-10042 is 2 days late",
                "10:42:09  ERROR  Payment service unreachable"));
        log.getStyleClass().add("monospace");
        return log;
    }

    private static Node problems(Action refresh) {
        EmptyState empty = new EmptyState();
        empty.setTitle("No problem");
        empty.setText("The orders have no problem left to solve");
        empty.getActions().setAll(refresh);
        return empty;
    }

    /**
     * A line of an order.
     *
     * @param item     what the line orders
     * @param quantity how many of it
     * @param price    the price of one, in francs
     */
    private record Line(String item, int quantity, BigDecimal price) {

        BigDecimal amount() {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    /**
     * The properties of the order selected, which the user edits: two of them fail their validation.
     */
    private static final class OrderForm {

        private final TextField customer = new TextField();
        private final TextField email = new TextField();
        private final DatePicker delivery = new DatePicker();
        private final VBox node;

        OrderForm() {
            ComboBox<String> currency = new ComboBox<>(FXCollections.observableArrayList("CHF", "EUR", "USD"));
            currency.setValue("EUR");
            Validation.setWarning(currency, "The shop invoices in CHF");
            CheckBox gift = new CheckBox("Gift wrapped");
            gift.setSelected(true);
            for (Region field : List.of(customer, email, currency, delivery)) {
                field.setMaxWidth(Double.MAX_VALUE);
            }
            Button save = new Button("Save");
            save.setDefaultButton(true);
            HBox buttons = new HBox(8, save, new Button("Revert"));
            Label saved = new Label("Saved 5 minutes ago");
            saved.getStyleClass().addAll("text-muted", "text-small");

            node = new VBox(6, label("Customer"), customer, label("Email"), email, label("Currency"), currency,
                    label("Delivery"), delivery, gift, buttons, saved);
            VBox.setMargin(gift, new Insets(6, 0, 4, 0));
            node.setPadding(new Insets(2, 12, 12, 12));
        }

        Node getNode() {
            return node;
        }

        /**
         * Shows the properties of an order, its address left without its domain.
         */
        void show(Order order) {
            customer.setText(order.customer());
            email.setText(order.customer().toLowerCase(Locale.ROOT).replace(' ', '.') + "@");
            Validation.setError(email, "The address misses its domain, such as example.com");
            delivery.setValue(order.placed().plusDays(5));
        }
    }
}
