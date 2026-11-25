package com.chavaillaz.fx.islands.workbench;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderDragType;
import javafx.scene.layout.Region;

/**
 * The toolbar along the top of the window: the widgets naming what the window shows on the left, the actions applying
 * to the whole window on the right.
 * <p>
 * It is the header bar of the window: in a window of the {@link javafx.stage.StageStyle#EXTENDED extended} style, it
 * stands in the title bar, beside the buttons of the operating system, and dragging its free room moves the window.
 * It carries the style class {@code main-toolbar}, and its two groups of items {@code toolbar-items}.
 */
public final class MainToolbar {

    /** The height of the toolbar in pixels, which the buttons of the operating system are given as well. */
    public static final double HEIGHT = 40;

    /** The size of the icons of the buttons of the toolbar, in pixels. */
    public static final double ICON_SIZE = 20;

    private final HeaderBar bar = new HeaderBar();
    private final HBox leftItems = new HBox();
    private final HBox rightItems = new HBox();

    /**
     * Creates a toolbar holding no item yet.
     */
    public MainToolbar() {
        bar.getStyleClass().add("main-toolbar");
        bar.setMinHeight(HEIGHT);
        bar.setPrefHeight(HEIGHT);
        leftItems.getStyleClass().addAll("toolbar-items", "toolbar-left");
        rightItems.getStyleClass().addAll("toolbar-items", "toolbar-right");
        // The room between the items moves the window as well, the items themselves handling their own clicks
        HeaderBar.setDragType(leftItems, HeaderDragType.DRAGGABLE);
        HeaderBar.setDragType(rightItems, HeaderDragType.DRAGGABLE);
        bar.setLeft(leftItems);
        bar.setRight(rightItems);
    }

    /**
     * Gives the items on the left of the toolbar, which name what the window shows.
     *
     * @return the modifiable list of the items, from left to right
     */
    public ObservableList<Node> getLeftItems() {
        return leftItems.getChildren();
    }

    /**
     * Gives the items on the right of the toolbar, which apply to the whole window.
     *
     * @return the modifiable list of the items, from left to right
     */
    public ObservableList<Node> getRightItems() {
        return rightItems.getChildren();
    }

    /**
     * Gives the node showing the toolbar.
     *
     * @return the header bar
     */
    public Region getNode() {
        return bar;
    }
}
