package com.chavaillaz.fx.islands.control;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import com.chavaillaz.fx.islands.action.Action;

/**
 * What an area shows while it has nothing to show: a title, a line telling what would fill the area, and links to the
 * actions helping to, each beside its shortcut.
 * <p>
 * It carries the style class {@code empty-state}, its title {@code empty-state-title}, its text
 * {@code empty-state-text}, its links {@code empty-state-action} and their shortcuts {@code empty-state-shortcut}.
 * A part left empty takes no room.
 */
public final class EmptyState extends VBox {

    private final Label title = new Label();
    private final Label text = new Label();
    private final GridPane actionGrid = new GridPane();
    private final ObservableList<Action> actions = FXCollections.observableArrayList();

    /**
     * Creates an empty state showing nothing yet.
     */
    public EmptyState() {
        getStyleClass().add("empty-state");
        setFillWidth(false);
        title.getStyleClass().add("empty-state-title");
        text.getStyleClass().add("empty-state-text");
        text.setWrapText(true);
        text.setTextAlignment(TextAlignment.CENTER);
        actionGrid.getStyleClass().add("empty-state-actions");
        title.managedProperty().bind(title.textProperty().isNotEmpty());
        text.managedProperty().bind(text.textProperty().isNotEmpty());
        actionGrid.managedProperty().bind(Bindings.isNotEmpty(actions));
        title.visibleProperty().bind(title.managedProperty());
        text.visibleProperty().bind(text.managedProperty());
        actionGrid.visibleProperty().bind(actionGrid.managedProperty());
        getChildren().addAll(title, text, actionGrid);
        actions.addListener((InvalidationListener) observable -> showActions());
    }

    /**
     * Gives the title, saying in a few words why the area is empty.
     *
     * @return the property of the title, empty for none
     */
    public StringProperty titleProperty() {
        return title.textProperty();
    }

    /**
     * Gives the title.
     *
     * @return the title, empty for none
     */
    public String getTitle() {
        return title.getText();
    }

    /**
     * Changes the title.
     *
     * @param title the title, empty for none
     */
    public void setTitle(String title) {
        this.title.setText(title);
    }

    /**
     * Gives the text under the title, telling what would fill the area.
     *
     * @return the property of the text, empty for none
     */
    public StringProperty textProperty() {
        return text.textProperty();
    }

    /**
     * Gives the text under the title.
     *
     * @return the text, empty for none
     */
    public String getText() {
        return text.getText();
    }

    /**
     * Changes the text under the title.
     *
     * @param text the text, empty for none
     */
    public void setText(String text) {
        this.text.setText(text);
    }

    /**
     * Gives the actions helping to fill the area, shown as links beside their shortcut.
     *
     * @return the modifiable list of the actions, in the order shown
     */
    public ObservableList<Action> getActions() {
        return actions;
    }

    private void showActions() {
        actionGrid.getChildren().clear();
        for (int row = 0; row < actions.size(); row++) {
            Action action = actions.get(row);
            Hyperlink link = new Hyperlink(action.getText());
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
