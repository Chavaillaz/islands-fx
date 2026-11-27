package com.chavaillaz.fx.islands.internal;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import org.jspecify.annotations.Nullable;

/**
 * The skin of a control showing a single node, which fills the control inside its padding: the sizes of the control
 * are those of the node, its padding added, which the skin of JavaFX leaves out once the node stands inside it.
 *
 * @param <C> the type of the control
 */
public abstract class FillingSkin<C extends Control> extends SkinBase<C> {

    /**
     * Creates the skin of a control.
     *
     * @param control the control
     */
    protected FillingSkin(C control) {
        super(control);
    }

    /**
     * Gives the node filling the control.
     *
     * @return the node, or {@code null} for none
     */
    protected abstract @Nullable Node node();

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        Node node = node();
        return left + (node == null ? 0 : node.minWidth(inside(height, top, bottom))) + right;
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        Node node = node();
        return left + (node == null ? 0 : node.prefWidth(inside(height, top, bottom))) + right;
    }

    @Override
    protected double computeMinHeight(double width, double top, double right, double bottom, double left) {
        Node node = node();
        return top + (node == null ? 0 : node.minHeight(inside(width, left, right))) + bottom;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        Node node = node();
        return top + (node == null ? 0 : node.prefHeight(inside(width, left, right))) + bottom;
    }

    /**
     * Gives a size inside the padding, or {@code -1} for a size not given.
     */
    private static double inside(double size, double before, double after) {
        return size < 0 ? -1 : Math.max(0, size - before - after);
    }
}
