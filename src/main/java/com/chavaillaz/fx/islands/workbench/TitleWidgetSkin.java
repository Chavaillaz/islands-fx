package com.chavaillaz.fx.islands.workbench;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import com.chavaillaz.fx.islands.internal.FillingSkin;

/**
 * The skin of a {@link TitleWidget}: in a row, the badge of the initials of its title, hidden while it has none, then
 * its title.
 */
final class TitleWidgetSkin extends FillingSkin<TitleWidget> {

    /** The gap between the badge and the title, in pixels. */
    static final double SPACING = 8;

    private final Label badge = new Label();
    private final Label name = new Label();
    private final HBox row = new HBox(SPACING, badge, name);

    TitleWidgetSkin(TitleWidget widget) {
        super(widget);
        badge.getStyleClass().add("title-badge");
        badge.textProperty().bind(widget.titleProperty().map(TitleWidget::initials));
        badge.visibleProperty().bind(widget.titleProperty().isNotEmpty());
        badge.managedProperty().bind(badge.visibleProperty());
        name.getStyleClass().add("title-name");
        name.textProperty().bind(widget.titleProperty());
        row.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void install() {
        getChildren().setAll(row);
    }

    @Override
    protected Node node() {
        return row;
    }

    @Override
    public void dispose() {
        badge.textProperty().unbind();
        badge.visibleProperty().unbind();
        name.textProperty().unbind();
        getChildren().remove(row);
        super.dispose();
    }
}
