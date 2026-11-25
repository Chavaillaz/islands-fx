package com.chavaillaz.fx.islands.control;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;

@ExtendWith(JavaFxExtension.class)
@DisplayName("EmptyState")
class EmptyStateTest {

    @Test
    @DisplayName("Takes no room for the parts left empty")
    void takesNoRoomForEmptyParts() {
        EmptyState emptyState = new EmptyState();

        assertThat(emptyState.getChildren()).noneMatch(Node::isManaged);

        emptyState.titleProperty().set("No record selected");
        assertThat(emptyState.getChildren()).filteredOn(Node::isManaged)
                .singleElement()
                .isInstanceOfSatisfying(Label.class,
                        title -> assertThat(title.getText()).isEqualTo("No record selected"));
    }

    @Test
    @DisplayName("Links to the actions helping, beside their shortcut")
    void linksToActions() {
        SimpleBooleanProperty performed = new SimpleBooleanProperty();
        KeyCombination accelerator = KeyCombination.keyCombination("Shortcut+R");
        EmptyState emptyState = new EmptyState();
        emptyState.textProperty().set("Select a record");
        emptyState.getActions().addAll(
                Action.builder("test.refresh", "Refresh").accelerator(accelerator)
                        .handler(() -> performed.set(true)).build(),
                Action.builder("test.retry", "Retry").handler(() -> { }).build());

        GridPane grid = (GridPane) emptyState.getChildren().get(2);
        assertThat(grid.isManaged()).isTrue();
        assertThat(grid.getChildren()).hasSize(4);
        Hyperlink link = (Hyperlink) grid.getChildren().get(0);
        assertThat(link.getText()).isEqualTo("Refresh");
        assertThat(((Label) grid.getChildren().get(1)).getText()).isEqualTo(accelerator.getDisplayText());
        assertThat(((Label) grid.getChildren().get(3)).getText()).isEmpty();

        link.fire();
        assertThat(performed.get()).isTrue();

        emptyState.getActions().clear();
        assertThat(grid.getChildren()).isEmpty();
        assertThat(grid.isManaged()).isFalse();
    }
}
