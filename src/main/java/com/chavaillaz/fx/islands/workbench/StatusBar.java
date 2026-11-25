package com.chavaillaz.fx.islands.workbench;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

/**
 * The bar along the bottom of the window: the path to what the window shows on the left, as breadcrumbs, and the
 * widgets telling how things stand on the right.
 * <p>
 * It carries the style class {@code status-bar}, its breadcrumbs {@code breadcrumb} and the group of its widgets
 * {@code status-widgets}.
 */
public final class StatusBar {

    private final HBox bar = new HBox();
    private final HBox breadcrumbs = new HBox();
    private final HBox widgets = new HBox();
    private final ObservableList<String> path = FXCollections.observableArrayList();

    /**
     * Creates a status bar showing no path and no widget yet.
     */
    public StatusBar() {
        bar.getStyleClass().add("status-bar");
        breadcrumbs.getStyleClass().add("breadcrumbs");
        widgets.getStyleClass().add("status-widgets");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(breadcrumbs, spacer, widgets);
        path.addListener((InvalidationListener) observable -> showPath());
    }

    /**
     * Gives the path to what the window shows, each element shown as a breadcrumb.
     *
     * @return the modifiable list of the elements, from the widest to the narrowest
     */
    public ObservableList<String> getPath() {
        return path;
    }

    /**
     * Gives the widgets of the status bar.
     *
     * @return the modifiable list of the widgets, from left to right
     */
    public ObservableList<Node> getWidgets() {
        return widgets.getChildren();
    }

    /**
     * Gives the node showing the status bar.
     *
     * @return the bar
     */
    public Region getNode() {
        return bar;
    }

    private void showPath() {
        List<Node> nodes = new ArrayList<>();
        for (String element : path) {
            if (!nodes.isEmpty()) {
                nodes.add(new IconView(Icons.CHEVRON_RIGHT));
            }
            Label breadcrumb = new Label(element);
            breadcrumb.getStyleClass().add("breadcrumb");
            nodes.add(breadcrumb);
        }
        breadcrumbs.getChildren().setAll(nodes);
    }
}
