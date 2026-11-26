package com.chavaillaz.fx.islands.control;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.StackPane;

import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

/**
 * The skin of a {@link SearchField}: the one of a text field, between a magnifier and a button clearing the text, the
 * room of both kept so that the text does not move when the button shows.
 * <p>
 * Both are laid out by the skin itself, left out of the children the size of the field is measured from, which would
 * otherwise widen the field at each layout.
 */
final class SearchFieldSkin extends TextFieldSkin {

    /** The gap between the text and the magnifier or the button, in pixels. */
    static final double GAP = 4;

    /** The size of the cross of the button, in pixels. */
    static final double CLEAR_SIZE = 12;

    private final IconView icon = new IconView(Icons.SEARCH);
    private final StackPane clear = new StackPane(new IconView(Icons.CLOSE, CLEAR_SIZE));

    SearchFieldSkin(SearchField field) {
        super(field);
        icon.getStyleClass().add("search-field-icon");
        icon.setManaged(false);
        clear.getStyleClass().add("search-field-clear");
        clear.setManaged(false);
        clear.setCursor(Cursor.DEFAULT);
        clear.visibleProperty().bind(field.textProperty().isNotEmpty());
        clear.setOnMouseClicked(event -> {
            field.clear();
            field.requestFocus();
            event.consume();
        });
        getChildren().addAll(icon, clear);
    }

    /**
     * Gives the button clearing the text, for the tests to click it.
     */
    StackPane getClearButton() {
        return clear;
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        double before = iconWidth();
        double after = clearWidth();
        super.layoutChildren(x + before, y, Math.max(0, width - before - after), height);
        layoutInArea(icon, x, y, before - GAP, height, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(clear, x + width - after + GAP, y, after - GAP, height, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return super.computePrefWidth(height, top, right, bottom, left) + iconWidth() + clearWidth();
    }

    @Override
    protected double computeMinWidth(double height, double top, double right, double bottom, double left) {
        return left + iconWidth() + clearWidth() + right;
    }

    private double iconWidth() {
        return snapSizeX(icon.prefWidth(-1)) + GAP;
    }

    private double clearWidth() {
        return snapSizeX(clear.prefWidth(-1)) + GAP;
    }
}
