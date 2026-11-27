package com.chavaillaz.fx.islands.control;

import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.internal.FillingSkin;

/**
 * The skin of an {@link EmptyState}: its title, its text and the grid of the links to its actions, stacked in the
 * middle, a part left empty taking no room.
 */
final class EmptyStateSkin extends FillingSkin<EmptyState> {

    /** The gap between the parts, in pixels. */
    static final double SPACING = 8;

    private final Label title = new Label();
    private final Label text = new Label();
    private final GridPane actionGrid = new GridPane();
    private final VBox column = new VBox(SPACING, title, text, actionGrid);

    EmptyStateSkin(EmptyState emptyState) {
        super(emptyState);
        title.getStyleClass().add("empty-state-title");
        title.textProperty().bind(emptyState.titleProperty());
        text.getStyleClass().add("empty-state-text");
        text.textProperty().bind(emptyState.textProperty());
        text.setWrapText(true);
        text.setTextAlignment(TextAlignment.CENTER);
        actionGrid.getStyleClass().add("empty-state-actions");
        title.managedProperty().bind(title.textProperty().isNotEmpty());
        text.managedProperty().bind(text.textProperty().isNotEmpty());
        actionGrid.managedProperty().bind(Bindings.isNotEmpty(emptyState.getActions()));
        title.visibleProperty().bind(title.managedProperty());
        text.visibleProperty().bind(text.managedProperty());
        actionGrid.visibleProperty().bind(actionGrid.managedProperty());
        column.setAlignment(Pos.CENTER);
        column.setFillWidth(false);
        showActions();
        registerListChangeListener(emptyState.getActions(), change -> showActions());
    }

    @Override
    public void install() {
        getChildren().setAll(column);
    }

    @Override
    protected Node node() {
        return column;
    }

    @Override
    public void dispose() {
        title.textProperty().unbind();
        text.textProperty().unbind();
        actionGrid.managedProperty().unbind();
        getChildren().remove(column);
        super.dispose();
    }

    private void showActions() {
        actionGrid.getChildren().clear();
        for (int row = 0; row < getSkinnable().getActions().size(); row++) {
            Action action = getSkinnable().getActions().get(row);
            Hyperlink link = new Hyperlink();
            link.textProperty().bind(action.textProperty());
            link.getStyleClass().add("empty-state-action");
            link.setMnemonicParsing(false);
            link.disableProperty().bind(action.disabledProperty());
            link.setOnAction(event -> action.perform());
            KeyCombination accelerator = action.getAccelerator();
            Label shortcut = new Label(accelerator == null ? "" : accelerator.getDisplayText());
            shortcut.getStyleClass().add("empty-state-shortcut");
            GridPane.setHalignment(link, HPos.RIGHT);
            actionGrid.addRow(row, link, shortcut);
        }
    }
}
