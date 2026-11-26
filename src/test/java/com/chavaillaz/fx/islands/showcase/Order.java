package com.chavaillaz.fx.islands.showcase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import com.chavaillaz.fx.islands.control.Badge;
import com.chavaillaz.fx.islands.control.Tone;

/**
 * An order the showcase lists, made up as its customer.
 *
 * @param number   the number of the order
 * @param customer the name of the customer
 * @param placed   the day the order was placed
 * @param status   where the order stands
 * @param amount   the amount to pay, in francs
 */
record Order(String number, String customer, LocalDate placed, Status status, BigDecimal amount) {

    /**
     * Gives the orders the showcase lists, placed in November 2026.
     *
     * @return the orders, from the oldest
     */
    static List<Order> samples() {
        return List.of(
                sample("SO-10030", "Donald Knuth", 5, Status.PAID, "64.00"),
                sample("SO-10031", "Frances Allen", 6, Status.PAID, "158.30"),
                sample("SO-10032", "Dennis Ritchie", 7, Status.PAID, "23.90"),
                sample("SO-10033", "Radia Perlman", 8, Status.LATE, "410.00"),
                sample("SO-10034", "John Backus", 9, Status.PAID, "76.45"),
                sample("SO-10035", "Hedy Lamarr", 10, Status.PENDING, "199.00"),
                sample("SO-10036", "Annie Easley", 11, Status.PAID, "35.60"),
                sample("SO-10037", "Ada Lovelace", 11, Status.PAID, "12.50"),
                sample("SO-10038", "Grace Hopper", 12, Status.PAID, "245.00"),
                sample("SO-10039", "Alan Turing", 14, Status.PAID, "89.90"),
                sample("SO-10040", "Katherine Johnson", 16, Status.PENDING, "312.40"),
                sample("SO-10041", "Margaret Hamilton", 18, Status.OPEN, "56.00"),
                sample("SO-10042", "Ada Lovelace", 20, Status.LATE, "129.90"),
                sample("SO-10043", "Edsger Dijkstra", 21, Status.LATE, "18.50"),
                sample("SO-10044", "Barbara Liskov", 23, Status.OPEN, "74.20"),
                sample("SO-10045", "Alan Turing", 24, Status.DRAFT, "42.00"));
    }

    private static Order sample(String number, String customer, int day, Status status, String amount) {
        return new Order(number, customer, LocalDate.of(2026, 11, day), status, new BigDecimal(amount));
    }

    /**
     * Gives the columns of a table of orders: their number, customer, status as a badge, and amount.
     *
     * @return the columns
     */
    static List<TableColumn<Order, ?>> columns() {
        TableColumn<Order, String> number = column("Number", Order::number, 90);
        TableColumn<Order, String> customer = column("Customer", Order::customer, 150);
        TableColumn<Order, Status> status = column("Status", Order::status, 80);
        status.setCellFactory(column -> new StatusCell());
        TableColumn<Order, String> amount = column("Amount", order -> order.amount().toPlainString(), 80);
        amount.getStyleClass().add("align-right");
        return List.of(number, customer, status, amount);
    }

    /**
     * Tells whether the order holds a text in its number or the name of its customer, ignoring the case.
     *
     * @param text the text, such as the one of a search field
     * @return {@code true} if the order holds the text, or if the text is blank
     */
    boolean matches(String text) {
        String query = text.strip().toLowerCase(Locale.ROOT);
        return number.toLowerCase(Locale.ROOT).contains(query) || customer.toLowerCase(Locale.ROOT).contains(query);
    }

    private static <T> TableColumn<Order, T> column(String title, Function<Order, T> value, double width) {
        TableColumn<Order, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    /**
     * Where an order stands, which a badge of its tone shows.
     */
    enum Status {

        /** Not placed yet. */
        DRAFT("Draft", Tone.NEUTRAL),

        /** Placed, waiting for its payment. */
        OPEN("Open", Tone.INFO),

        /** Paid. */
        PAID("Paid", Tone.SUCCESS),

        /** Paid, its payment not confirmed yet. */
        PENDING("Pending", Tone.WARNING),

        /** Not paid in time. */
        LATE("Late", Tone.DANGER);

        private final String text;
        private final Tone tone;

        Status(String text, Tone tone) {
            this.text = text;
            this.tone = tone;
        }

        /**
         * Creates the badge showing the status.
         *
         * @return the badge
         */
        Badge badge() {
            Badge badge = new Badge();
            badge.update(text, tone);
            return badge;
        }
    }

    /**
     * Shows the status of an order as a badge.
     */
    private static final class StatusCell extends TableCell<Order, Status> {

        @Override
        protected void updateItem(Status status, boolean empty) {
            super.updateItem(status, empty);
            setGraphic(empty || status == null ? null : status.badge());
        }
    }
}
