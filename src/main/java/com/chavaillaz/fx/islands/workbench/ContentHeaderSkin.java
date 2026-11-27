package com.chavaillaz.fx.islands.workbench;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.internal.FillingSkin;

/**
 * The skin of a {@link ContentHeader}: in a row, its tab, hidden while it has no title, then the buttons of its
 * actions at the end.
 */
final class ContentHeaderSkin extends FillingSkin<ContentHeader> {

    /** The gap between the parts of the row, in pixels. */
    static final double SPACING = 2;

    private final Label tab = new Label();
    private final Region spacer = new Region();
    private final HBox row = new HBox(SPACING);

    ContentHeaderSkin(ContentHeader header) {
        super(header);
        tab.getStyleClass().add("content-tab");
        tab.setMnemonicParsing(false);
        tab.textProperty().bind(header.titleProperty());
        tab.visibleProperty().bind(tab.textProperty().isNotEmpty());
        tab.managedProperty().bind(tab.visibleProperty());
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);
        showIcon();
        showActions();
        registerChangeListener(header.iconProperty(), observable -> showIcon());
        registerListChangeListener(header.getActions(), change -> showActions());
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
        tab.textProperty().unbind();
        getChildren().remove(row);
        super.dispose();
    }

    private void showIcon() {
        Icon icon = getSkinnable().getIcon();
        tab.setGraphic(icon == null ? null : new IconView(icon));
    }

    private void showActions() {
        row.getChildren().setAll(tab, spacer);
        getSkinnable().getActions().forEach(action -> row.getChildren().add(ActionButtons.create(action)));
    }
}
