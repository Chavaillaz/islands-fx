package com.chavaillaz.fx.islands.control;

import java.util.function.Function;

import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;
import org.jspecify.annotations.Nullable;

/**
 * Tints the rows of a table after the {@link Tone} of their item, as badges are, such as the late orders in red: the
 * row under the mouse and the selection still show over the tint.
 * <p>
 * A row tinted carries the pseudo-class of its tone, such as {@code :danger}, which the stylesheets turn into the
 * looked-up color {@code -row-tint} its background is painted with. A row is tinted again when its item changes, a
 * table refreshing its rows for a change within an item.
 */
public final class ToneRows {

    private ToneRows() {
    }

    /**
     * Creates the rows of a table, tinted after the tone of their item.
     *
     * @param tone gives the tone of an item, or {@code null} for an item left untinted
     * @param <T>  the type of the items
     * @return the factory of the rows, to give the table
     */
    public static <T> Callback<TableView<T>, TableRow<T>> forTable(Function<? super T, @Nullable Tone> tone) {
        return table -> new TableRow<>() {
            @Override
            protected void updateItem(@Nullable T item, boolean empty) {
                super.updateItem(item, empty);
                tint(this, empty || item == null ? null : tone.apply(item));
            }
        };
    }

    /**
     * Creates the rows of a tree table, tinted after the tone of their item.
     *
     * @param tone gives the tone of an item, or {@code null} for an item left untinted
     * @param <T>  the type of the items
     * @return the factory of the rows, to give the tree table
     */
    public static <T> Callback<TreeTableView<T>, TreeTableRow<T>> forTreeTable(
            Function<? super T, @Nullable Tone> tone) {
        return table -> new TreeTableRow<>() {
            @Override
            protected void updateItem(@Nullable T item, boolean empty) {
                super.updateItem(item, empty);
                tint(this, empty || item == null ? null : tone.apply(item));
            }
        };
    }

    private static void tint(IndexedCell<?> row, @Nullable Tone tone) {
        for (Tone each : Tone.values()) {
            row.pseudoClassStateChanged(each.pseudoClass(), each == tone);
        }
    }
}
