package com.chavaillaz.fx.islands.workbench;

import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.VBox;

import com.chavaillaz.fx.islands.action.ActionButtons;

/**
 * The column of tool window buttons along a side of the workbench, each showing and hiding its tool window. It takes
 * no room while it holds no button.
 * <p>
 * It carries the style classes {@code stripe} and {@code stripe-left} or {@code stripe-right}, its buttons
 * {@code stripe-button}, selected while their tool window shows.
 */
final class ToolWindowStripe extends VBox {

    /** The size of the icons of the buttons, in pixels. */
    static final double ICON_SIZE = 20;

    ToolWindowStripe(ToolWindowAnchor anchor) {
        getStyleClass().addAll("stripe", "stripe-" + anchor.name().toLowerCase(Locale.ROOT));
        BooleanBinding holdsButtons = Bindings.isNotEmpty(getChildren());
        visibleProperty().bind(holdsButtons);
        managedProperty().bind(holdsButtons);
    }

    /**
     * Adds the button of a tool window, after the buttons already there.
     *
     * @param toolWindow the tool window
     */
    void add(ToolWindow toolWindow) {
        ButtonBase button = ActionButtons.create(toolWindow.getToggleAction(), ICON_SIZE);
        button.getStyleClass().add("stripe-button");
        getChildren().add(button);
    }
}
