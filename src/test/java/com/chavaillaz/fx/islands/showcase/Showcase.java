package com.chavaillaz.fx.islands.showcase;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import javafx.application.Platform;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.theme.ThemeManager;
import com.chavaillaz.fx.islands.theme.ThemeMode;
import com.chavaillaz.fx.islands.workbench.ContentHeader;
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

    private final Workbench workbench;
    private final ThemeManager themes;
    private final TaskStatus status = new TaskStatus();
    private final ToolWindow log;
    private final Action retry;
    private final Action details;

    /**
     * Lays the main window out in a workbench.
     *
     * @param workbench the workbench of the main window
     * @param themes    the manager of the themes, which the menus offer to change
     */
    public Showcase(Workbench workbench, ThemeManager themes) {
        this.workbench = workbench;
        this.themes = themes;
        List<Order> orders = Order.samples();
        Order order = orders.stream().filter(each -> each.number().equals("SO-10042")).findFirst().orElseThrow();

        Action newOrder = action("orders.new", "New Order", Icons.ADD, "Shortcut+N",
                () -> tell(Tone.INFO, "Order SO-10046 created"));
        Action refresh = action("orders.refresh", "Refresh Orders", Icons.REFRESH, "Shortcut+R", this::refresh);
        Action export = action("orders.export", "Export Orders", Icons.EXPORT, null,
                () -> tell(Tone.SUCCESS, "Orders exported to orders.csv"));
        Action edit = action("order.edit", "Edit Order", Icons.EDIT, "Shortcut+E", () -> { });
        Action copy = action("order.copy", "Copy Order Number", Icons.COPY, null, () -> { });
        Action delete = action("order.delete", "Delete Order", Icons.DELETE, null,
                () -> tell(Tone.WARNING, "Order " + order.number() + " deleted"));
        Action gallery = action("showcase.gallery", "Control Gallery", Icons.BOOK, "Shortcut+G", this::showGallery);
        retry = action("payment.retry", "Retry", Icons.REFRESH, null, this::refresh);
        details = action("payment.details", "Show the Log", Icons.LIST, null, () -> getLog().setVisible(true));

        ToolWindow ordersWindow = ToolWindow.builder("orders", "Orders", Icons.RECORDS, orders(orders, order))
                .accelerator(KeyCombination.keyCombination("Shortcut+1"))
                .width(420)
                .actions(newOrder, refresh)
                .build();
        ToolWindow customers = ToolWindow.builder("customers", "Customers", Icons.FOLDER, customers(orders))
                .accelerator(KeyCombination.keyCombination("Shortcut+2"))
                .build();
        ToolWindow properties = ToolWindow.builder("properties", "Properties", Icons.PROPERTY, properties(order))
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

        ContentHeader header = new ContentHeader(order.number(), Icons.RECORD);
        header.getActions().setAll(edit, copy, delete);
        BorderPane content = new BorderPane(details(order));
        content.setTop(header);
        workbench.setContent(content);

        workbench.getMainToolbar().getLeftItems().addAll(List.of(new TitleWidget("Order Desk"),
                ActionButtons.create(newOrder), ActionButtons.create(refresh), ActionButtons.create(export)));
        workbench.getMainToolbar().getRightItems().addAll(List.of(ActionButtons.create(workbench.getSearchAction()),
                ActionButtons.create(gallery)));
        workbench.getStatusBar().getPath().setAll("Orders", order.number());
        workbench.getStatusBar().getWidgets().setAll(List.of(status, new Label(orders.size() + " orders")));
        workbench.setMainMenu(ActionMenus.createMenus(List.of(
                ActionGroup.of("File", newOrder, refresh, export),
                ActionGroup.of("Order", edit, copy, delete),
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
     * Creates the content of the tool window of the orders: a field searching them, above their table.
     */
    private static Node orders(List<Order> orders, Order selected) {
        SearchField search = new SearchField();
        search.setPromptText("Search orders");
        FilteredList<Order> found = new FilteredList<>(FXCollections.observableArrayList(orders));
        search.textProperty().subscribe(text -> found.setPredicate(order -> order.matches(text)));

        TableView<Order> table = new TableView<>(found);
        table.getColumns().setAll(Order.columns());
        table.setRowFactory(ToneRows.forTable(order -> order.status() == Order.Status.LATE ? Tone.DANGER : null));
        table.getStyleClass().add("edge-to-edge");
        table.getSelectionModel().select(selected);
        VBox.setMargin(search, new Insets(0, 8, 0, 8));
        VBox.setVgrow(table, Priority.ALWAYS);
        return new VBox(6, search, table);
    }

    private static Node customers(List<Order> orders) {
        return new ListView<>(FXCollections.observableArrayList(
                orders.stream().map(Order::customer).distinct().sorted().toList()));
    }

    /**
     * Creates the details of an order: its customer, its status, the lines it orders, and its delivery note.
     */
    private static Node details(Order order) {
        Label customer = new Label(order.customer());
        customer.getStyleClass().addAll("text-large", "text-bold");
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox title = new HBox(10, customer, space, order.status().badge());
        title.setAlignment(Pos.CENTER_LEFT);
        Label placed = new Label("Placed on 20 November 2026, to deliver by 25 November 2026");
        placed.getStyleClass().add("text-secondary");

        TableView<Line> lines = new TableView<>(FXCollections.observableArrayList(
                new Line("Mechanical keyboard", 1, new BigDecimal("89.00")),
                new Line("USB-C cable", 2, new BigDecimal("12.50")),
                new Line("Desk mat", 1, new BigDecimal("15.90"))));
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
        ListView<String> history = new ListView<>(FXCollections.observableArrayList(
                "20 Nov 2026, 10:12    Placed online",
                "20 Nov 2026, 10:13    Invoice sent",
                "23 Nov 2026, 08:00    Payment reminder sent",
                "25 Nov 2026, 08:00    Payment overdue"));
        history.setPrefHeight(110);
        VBox.setVgrow(history, Priority.ALWAYS);

        VBox details = new VBox(10, title, placed, lines, totals, label("Delivery note"), note, label("History"),
                history);
        details.setPadding(new Insets(10, 16, 16, 16));
        return details;
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

    /**
     * Creates the properties of an order, which the user edits: two of them fail their validation.
     */
    private static Node properties(Order order) {
        TextField customer = new TextField(order.customer());
        TextField email = new TextField("ada.lovelace@");
        Validation.setError(email, "The address misses its domain, such as example.com");
        ComboBox<String> currency = new ComboBox<>(FXCollections.observableArrayList("CHF", "EUR", "USD"));
        currency.setValue("EUR");
        Validation.setWarning(currency, "The shop invoices in CHF");
        DatePicker delivery = new DatePicker(order.placed().plusDays(5));
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

        VBox form = new VBox(6, label("Customer"), customer, label("Email"), email, label("Currency"), currency,
                label("Delivery"), delivery, gift, buttons, saved);
        VBox.setMargin(gift, new Insets(6, 0, 4, 0));
        form.setPadding(new Insets(2, 12, 12, 12));
        return form;
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
}
