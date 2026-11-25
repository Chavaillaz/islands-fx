package com.chavaillaz.fx.islands.workbench;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * Lays out the islands of the workbench side by side: the tool windows docked on the left, the content, then the tool
 * windows docked on the right, separated by gaps.
 * <p>
 * A tool window takes its width while the room allows, the content taking what is left and never less than
 * {@link #MIN_CONTENT_WIDTH} while the tool windows can narrow. The first tool window docked on a side stands the
 * farthest from the content. Dragging the gap beside a tool window resizes it.
 */
final class IslandsPane extends Region {

    /** The gap between two islands, in pixels. */
    static final double GAP = 6;

    /** The narrowest the content gets while the tool windows can narrow, in pixels. */
    static final double MIN_CONTENT_WIDTH = 240;

    private final Node content;
    private final List<Docked> docked = new ArrayList<>();

    IslandsPane(Node content) {
        this.content = content;
        getStyleClass().add("islands");
        getChildren().add(content);
    }

    /**
     * Docks a tool window beside the content, shown while the tool window is visible.
     *
     * @param toolWindow the tool window
     * @param island     the island showing it
     */
    void dock(ToolWindow toolWindow, Node island) {
        Divider divider = new Divider(toolWindow, island);
        docked.add(new Docked(toolWindow, island, divider));
        getChildren().addAll(island, divider);
        for (Node node : List.of(island, divider)) {
            node.visibleProperty().bind(toolWindow.visibleProperty());
            node.managedProperty().bind(toolWindow.visibleProperty());
        }
        toolWindow.widthProperty().addListener(observable -> requestLayout());
    }

    @Override
    protected void layoutChildren() {
        double top = snappedTopInset();
        double height = getHeight() - top - snappedBottomInset();
        double left = snappedLeftInset();
        double right = getWidth() - snappedRightInset();
        double room = right - left - MIN_CONTENT_WIDTH;
        for (Docked each : shown(ToolWindowAnchor.LEFT)) {
            double width = allot(each.toolWindow(), room);
            each.island().resizeRelocate(left, top, width, height);
            each.divider().resizeRelocate(left + width, top, GAP, height);
            left += width + GAP;
            room -= width + GAP;
        }
        for (Docked each : shown(ToolWindowAnchor.RIGHT)) {
            double width = allot(each.toolWindow(), room);
            each.island().resizeRelocate(right - width, top, width, height);
            each.divider().resizeRelocate(right - width - GAP, top, GAP, height);
            right -= width + GAP;
            room -= width + GAP;
        }
        content.resizeRelocate(left, top, Math.max(0, right - left), height);
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + MIN_CONTENT_WIDTH + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        double width = snappedLeftInset() + MIN_CONTENT_WIDTH + snappedRightInset();
        for (Docked each : docked) {
            if (each.toolWindow().isVisible()) {
                width += Math.max(each.toolWindow().getWidth(), ToolWindow.MIN_WIDTH) + GAP;
            }
        }
        return width;
    }

    @Override
    protected double computeMinHeight(double width) {
        return snappedTopInset() + snappedBottomInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        double height = 0;
        for (Node child : getManagedChildren()) {
            height = Math.max(height, child.prefHeight(-1));
        }
        return snappedTopInset() + height + snappedBottomInset();
    }

    private List<Docked> shown(ToolWindowAnchor anchor) {
        return docked.stream()
                .filter(each -> each.toolWindow().getAnchor() == anchor && each.toolWindow().isVisible())
                .toList();
    }

    /**
     * Gives the width of a tool window, the one it takes as long as the room left beside the content allows.
     */
    private double allot(ToolWindow toolWindow, double room) {
        double wanted = Math.max(toolWindow.getWidth(), ToolWindow.MIN_WIDTH);
        return snapSizeX(Math.max(0, Math.min(wanted, room - GAP)));
    }

    /**
     * Gives the widest a tool window can get, beside the other tool windows showing and the content.
     */
    private double maxToolWindowWidth(ToolWindow toolWindow) {
        double room = getWidth() - snappedLeftInset() - snappedRightInset() - MIN_CONTENT_WIDTH - GAP;
        for (Docked each : docked) {
            if (each.toolWindow() != toolWindow && each.toolWindow().isVisible()) {
                room -= each.island().getLayoutBounds().getWidth() + GAP;
            }
        }
        return Math.max(ToolWindow.MIN_WIDTH, room);
    }

    private record Docked(ToolWindow toolWindow, Node island, Divider divider) {
    }

    /**
     * The gap beside a tool window, dragged to resize it.
     */
    private final class Divider extends Region {

        private double pressedX;
        private double pressedWidth;

        private Divider(ToolWindow toolWindow, Node island) {
            getStyleClass().add("island-divider");
            setCursor(Cursor.H_RESIZE);
            setOnMousePressed(event -> {
                pressedX = event.getScreenX();
                pressedWidth = island.getLayoutBounds().getWidth();
                event.consume();
            });
            setOnMouseDragged(event -> {
                double moved = event.getScreenX() - pressedX;
                double width = toolWindow.getAnchor() == ToolWindowAnchor.LEFT
                        ? pressedWidth + moved
                        : pressedWidth - moved;
                toolWindow.setWidth(Math.clamp(width, ToolWindow.MIN_WIDTH, maxToolWindowWidth(toolWindow)));
                event.consume();
            });
        }
    }
}
