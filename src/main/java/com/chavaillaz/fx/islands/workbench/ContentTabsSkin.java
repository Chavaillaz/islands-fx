package com.chavaillaz.fx.islands.workbench;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.ActionButtons;

/**
 * The skin of {@link ContentTabs}: the one of a tab pane, the area of its tab headers narrowed to leave the room of the
 * bar of its actions at its end, and closing the tab a click of the middle button lands on.
 */
final class ContentTabsSkin extends TabPaneSkin {

    /** The gap between the buttons of the actions, and between the last tab and the first button, in pixels. */
    static final double SPACING = 2;

    private final HBox actionBar = new HBox(SPACING);
    private final EventHandler<MouseEvent> closeOnMiddleClick = this::closeOnMiddleClick;

    ContentTabsSkin(ContentTabs tabs) {
        super(tabs);
        actionBar.getStyleClass().add("content-tabs-actions");
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        showActions();
        registerListChangeListener(tabs.getActions(), change -> showActions());
        tabs.addEventHandler(MouseEvent.MOUSE_CLICKED, closeOnMiddleClick);
    }

    @Override
    public void install() {
        super.install();
        getChildren().add(actionBar);
    }

    @Override
    public void dispose() {
        getSkinnable().removeEventHandler(MouseEvent.MOUSE_CLICKED, closeOnMiddleClick);
        getChildren().remove(actionBar);
        super.dispose();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        Region header = headerArea();
        boolean shown = header != null && !actionBar.getChildren().isEmpty();
        actionBar.setVisible(shown);
        if (shown) {
            double width = snapSizeX(actionBar.prefWidth(-1));
            double height = snapSizeY(actionBar.prefHeight(width));
            double right = header.getLayoutX() + header.getWidth() - header.snappedRightInset();
            header.resize(Math.max(0, header.getWidth() - width - SPACING), header.getHeight());
            actionBar.resizeRelocate(right - width, header.getLayoutY() + (header.getHeight() - height) / 2,
                    width, height);
        }
    }

    private @Nullable Region headerArea() {
        for (Node child : getChildren()) {
            if (child instanceof Region region && child.getStyleClass().contains("tab-header-area")) {
                return region;
            }
        }
        return null;
    }

    private void showActions() {
        actionBar.getChildren().clear();
        ((ContentTabs) getSkinnable()).getActions()
                .forEach(action -> actionBar.getChildren().add(ActionButtons.create(action)));
        getSkinnable().requestLayout();
    }

    /**
     * Closes the tab a click of the middle button lands on, unless it cannot be closed.
     */
    private void closeOnMiddleClick(MouseEvent event) {
        TabPane tabs = getSkinnable();
        if (event.getButton() != MouseButton.MIDDLE
                || tabs.getTabClosingPolicy() == TabPane.TabClosingPolicy.UNAVAILABLE) {
            return;
        }
        for (Node node = event.getTarget() instanceof Node target ? target : null; node != null && node != tabs;
             node = node.getParent()) {
            if (node.getProperties().get(Tab.class) instanceof Tab tab) {
                if (tab.isClosable() && !tab.isDisabled()) {
                    ((ContentTabs) tabs).close(tab);
                    event.consume();
                }
                return;
            }
        }
    }
}
