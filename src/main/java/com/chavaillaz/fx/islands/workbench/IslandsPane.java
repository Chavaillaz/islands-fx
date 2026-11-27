package com.chavaillaz.fx.islands.workbench;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.util.Subscription;

/**
 * Lays out the islands of the workbench: the tool windows docked on the left, the content, then the tool windows docked
 * on the right, side by side, and the tool window docked at the bottom under the content, between those of the sides,
 * separated by gaps.
 * <p>
 * A tool window takes its width, or its height at the bottom, while the room allows, the content taking what is left
 * and never less than {@link #MIN_CONTENT_WIDTH} by {@link #MIN_CONTENT_HEIGHT} while the tool windows can shrink. The
 * first tool window docked on a side stands the farthest from the content. Dragging the gap beside a tool window, or
 * above it at the bottom, resizes it.
 */
final class IslandsPane extends Region {

    /** The gap between two islands, in pixels. */
    static final double GAP = 6;

    /** The narrowest the content gets while the tool windows can narrow, in pixels. */
    static final double MIN_CONTENT_WIDTH = 240;

    /** The lowest the content gets while a tool window at the bottom can shrink, in pixels. */
    static final double MIN_CONTENT_HEIGHT = 120;

    private final Node content;
    private final List<Docked> docked = new ArrayList<>();

    IslandsPane(Node content) {
        this.content = content;
        getStyleClass().add("islands");
        getChildren().add(content);
    }

    /**
     * Docks a tool window beside the content, or under it, shown while the tool window is visible.
     *
     * @param toolWindow the tool window
     * @param island     the island showing it
     */
    void dock(ToolWindow toolWindow, Node island) {
        Divider divider = new Divider(toolWindow, island);
        Subscription sizes = toolWindow.widthProperty().subscribe(this::requestLayout)
                .and(toolWindow.heightProperty().subscribe(this::requestLayout));
        docked.add(new Docked(toolWindow, island, divider, sizes));
        getChildren().addAll(island, divider);
        for (Node node : List.of(island, divider)) {
            node.visibleProperty().bind(toolWindow.visibleProperty());
            node.managedProperty().bind(toolWindow.visibleProperty());
        }
    }

    /**
     * Undocks a tool window, its island and the gap beside it leaving the pane.
     *
     * @param toolWindow the tool window, docked or not
     */
    void undock(ToolWindow toolWindow) {
        docked.removeIf(each -> {
            if (each.toolWindow() != toolWindow) {
                return false;
            }
            each.sizes().unsubscribe();
            for (Node node : List.of(each.island(), each.divider())) {
                node.visibleProperty().unbind();
                node.managedProperty().unbind();
            }
            getChildren().removeAll(each.island(), each.divider());
            return true;
        });
    }

    @Override
    protected void layoutChildren() {
        double top = snappedTopInset();
        double height = getHeight() - top - snappedBottomInset();
        double left = snappedLeftInset();
        double right = getWidth() - snappedRightInset();
        double room = right - left - MIN_CONTENT_WIDTH;
        for (Docked each : shown(ToolWindowAnchor.LEFT)) {
            double width = allotWidth(each.toolWindow(), room);
            each.island().resizeRelocate(left, top, width, height);
            each.divider().resizeRelocate(left + width, top, GAP, height);
            left += width + GAP;
            room -= width + GAP;
        }
        for (Docked each : shown(ToolWindowAnchor.RIGHT)) {
            double width = allotWidth(each.toolWindow(), room);
            each.island().resizeRelocate(right - width, top, width, height);
            each.divider().resizeRelocate(right - width - GAP, top, GAP, height);
            right -= width + GAP;
            room -= width + GAP;
        }
        double width = Math.max(0, right - left);
        double contentHeight = height;
        for (Docked each : shown(ToolWindowAnchor.BOTTOM)) {
            double bottomHeight = allotHeight(each.toolWindow(), contentHeight - MIN_CONTENT_HEIGHT);
            contentHeight -= bottomHeight + GAP;
            each.island().resizeRelocate(left, top + contentHeight + GAP, width, bottomHeight);
            each.divider().resizeRelocate(left, top + contentHeight, width, GAP);
        }
        content.resizeRelocate(left, top, width, Math.max(0, contentHeight));
    }

    @Override
    protected double computeMinWidth(double height) {
        return snappedLeftInset() + MIN_CONTENT_WIDTH + snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double height) {
        double width = snappedLeftInset() + MIN_CONTENT_WIDTH + snappedRightInset();
        for (Docked each : docked) {
            if (each.toolWindow().isVisible() && each.toolWindow().getAnchor() != ToolWindowAnchor.BOTTOM) {
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
     * Gives the width of a tool window on a side, the one it takes as long as the room left beside the content allows.
     */
    private double allotWidth(ToolWindow toolWindow, double room) {
        double wanted = Math.max(toolWindow.getWidth(), ToolWindow.MIN_WIDTH);
        return snapSizeX(Math.max(0, Math.min(wanted, room - GAP)));
    }

    /**
     * Gives the height of a tool window at the bottom, the one it takes as long as the room left above it allows.
     */
    private double allotHeight(ToolWindow toolWindow, double room) {
        double wanted = Math.max(toolWindow.getHeight(), ToolWindow.MIN_HEIGHT);
        return snapSizeY(Math.max(0, Math.min(wanted, room - GAP)));
    }

    /**
     * Gives the widest a tool window on a side can get, beside the other tool windows showing and the content.
     */
    private double maxToolWindowWidth(ToolWindow toolWindow) {
        double room = getWidth() - snappedLeftInset() - snappedRightInset() - MIN_CONTENT_WIDTH - GAP;
        for (Docked each : docked) {
            if (each.toolWindow() != toolWindow && each.toolWindow().isVisible()
                    && each.toolWindow().getAnchor() != ToolWindowAnchor.BOTTOM) {
                room -= each.island().getLayoutBounds().getWidth() + GAP;
            }
        }
        return Math.max(ToolWindow.MIN_WIDTH, room);
    }

    /**
     * Gives the highest a tool window at the bottom can get, under the content.
     */
    private double maxToolWindowHeight() {
        double room = getHeight() - snappedTopInset() - snappedBottomInset() - MIN_CONTENT_HEIGHT - GAP;
        return Math.max(ToolWindow.MIN_HEIGHT, room);
    }

    private record Docked(ToolWindow toolWindow, Node island, Divider divider, Subscription sizes) {
    }

    /**
     * The gap beside a tool window, or above it at the bottom, dragged to resize it.
     */
    private final class Divider extends Region {

        private double pressedAt;
        private double pressedSize;

        private Divider(ToolWindow toolWindow, Node island) {
            boolean bottom = toolWindow.getAnchor() == ToolWindowAnchor.BOTTOM;
            getStyleClass().add("island-divider");
            setCursor(bottom ? Cursor.V_RESIZE : Cursor.H_RESIZE);
            setOnMousePressed(event -> {
                pressedAt = bottom ? event.getScreenY() : event.getScreenX();
                pressedSize = bottom ? island.getLayoutBounds().getHeight() : island.getLayoutBounds().getWidth();
                event.consume();
            });
            setOnMouseDragged(event -> {
                double moved = (bottom ? event.getScreenY() : event.getScreenX()) - pressedAt;
                switch (toolWindow.getAnchor()) {
                    case LEFT -> toolWindow.setWidth(
                            Math.clamp(pressedSize + moved, ToolWindow.MIN_WIDTH, maxToolWindowWidth(toolWindow)));
                    case RIGHT -> toolWindow.setWidth(
                            Math.clamp(pressedSize - moved, ToolWindow.MIN_WIDTH, maxToolWindowWidth(toolWindow)));
                    case BOTTOM -> toolWindow.setHeight(
                            Math.clamp(pressedSize - moved, ToolWindow.MIN_HEIGHT, maxToolWindowHeight()));
                }
                event.consume();
            });
        }
    }
}
