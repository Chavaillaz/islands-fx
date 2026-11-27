package com.chavaillaz.fx.islands.workbench;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.internal.FillingSkin;

/**
 * The skin of an {@link Island}: its content as its only child, filling it, so that the stylesheets reach the content
 * as a child of the island, such as a list left without a frame there.
 */
final class IslandSkin extends FillingSkin<Island> {

    IslandSkin(Island island) {
        super(island);
        registerChangeListener(island.contentProperty(), observable -> showContent());
    }

    @Override
    public void install() {
        showContent();
    }

    @Override
    protected @Nullable Node node() {
        return getSkinnable().getContent();
    }

    @Override
    public void dispose() {
        getChildren().clear();
        super.dispose();
    }

    private void showContent() {
        Node content = getSkinnable().getContent();
        if (content == null) {
            getChildren().clear();
        } else {
            getChildren().setAll(content);
        }
    }
}
