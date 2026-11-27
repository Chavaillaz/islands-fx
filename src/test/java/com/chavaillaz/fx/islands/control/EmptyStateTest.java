package com.chavaillaz.fx.islands.control;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;

@ExtendWith(JavaFxExtension.class)
@DisplayName("EmptyState")
class EmptyStateTest {

    /**
     * Gives an empty state its skin, which a scene creates once styling it.
     */
    private static EmptyState skinned(EmptyState emptyState) {
        new Scene(new StackPane(emptyState));
        emptyState.applyCss();
        return emptyState;
    }

    @Test
    @DisplayName("Takes no room for the parts left empty")
    void takesNoRoomForEmptyParts() {
        EmptyState emptyState = skinned(new EmptyState());
        Node title = emptyState.lookup(".empty-state-title");

        assertThat(emptyState.getSkin()).isInstanceOf(EmptyStateSkin.class);
        assertThat(emptyState.isFocusTraversable()).isFalse();
        assertThat(List.of(title, emptyState.lookup(".empty-state-text"), emptyState.lookup(".empty-state-actions")))
                .noneMatch(Node::isManaged);

        emptyState.setTitle("No record selected");
        assertThat(emptyState.getTitle()).isEqualTo("No record selected");
        assertThat(title.isManaged()).isTrue();
        assertThat(((Label) title).getText()).isEqualTo("No record selected");
    }

    @Test
    @DisplayName("Links to the actions helping, beside their shortcut")
    void linksToActions() {
        SimpleBooleanProperty performed = new SimpleBooleanProperty();
        KeyCombination accelerator = KeyCombination.keyCombination("Shortcut+R");
        EmptyState emptyState = skinned(new EmptyState());
        emptyState.setText("Select a record");
        assertThat(emptyState.getText()).isEqualTo("Select a record");
        emptyState.getActions().addAll(
                Action.builder("test.refresh", "Refresh").accelerator(accelerator)
                        .handler(() -> performed.set(true)).build(),
                Action.builder("test.retry", "Retry").handler(() -> { }).build());

        GridPane grid = (GridPane) emptyState.lookup(".empty-state-actions");
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

    @Test
    @DisplayName("Leaves nothing of its skin behind once given another one")
    void replacesItsSkin() {
        EmptyState emptyState = skinned(new EmptyState());
        emptyState.setSkin(new EmptyStateSkin(emptyState));

        assertThat(emptyState.lookupAll(".empty-state-title")).hasSize(1);
    }
}
