package com.chavaillaz.fx.islands.control;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.collections.FXCollections;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
@DisplayName("ToneRows")
class ToneRowsTest {

    @Test
    @DisplayName("Tints a row of a table after the tone of its item, and again when its item changes")
    void tintsTableRows() {
        TableView<String> table = new TableView<>(FXCollections.observableArrayList("late", "paid"));
        TableRow<String> row = ToneRows.<String>forTable(item -> item.equals("late") ? Tone.DANGER : null).call(table);
        row.updateTableView(table);

        row.updateIndex(0);
        assertThat(row.getPseudoClassStates()).contains(Tone.DANGER.pseudoClass());

        row.updateIndex(1);
        assertThat(row.getPseudoClassStates()).doesNotContain(Tone.DANGER.pseudoClass());

        row.updateIndex(5);
        assertThat(row.getPseudoClassStates()).doesNotContain(Tone.DANGER.pseudoClass());
    }

    @Test
    @DisplayName("Tints a row of a tree table after the tone of its item")
    void tintsTreeTableRows() {
        TreeItem<String> root = new TreeItem<>("orders");
        root.getChildren().add(new TreeItem<>("late"));
        root.setExpanded(true);
        TreeTableView<String> table = new TreeTableView<>(root);
        TreeTableRow<String> row = ToneRows.<String>forTreeTable(item -> Tone.WARNING).call(table);
        row.updateTreeTableView(table);

        row.updateIndex(1);
        assertThat(row.getPseudoClassStates()).contains(Tone.WARNING.pseudoClass());
    }
}
