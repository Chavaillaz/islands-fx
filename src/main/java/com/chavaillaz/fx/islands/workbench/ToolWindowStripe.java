package com.chavaillaz.fx.islands.workbench;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import com.chavaillaz.fx.islands.action.ActionButtons;

/**
 * The column of tool window buttons along a side of the workbench, each showing and hiding its tool window: those of
 * the tool windows docked on that side at its top, and on the left, those of the tool windows docked at the bottom at
 * its bottom. It takes no room while it holds no button.
 * <p>
 * It carries the style classes {@code stripe} and {@code stripe-left} or {@code stripe-right}, its two groups of
 * buttons {@code stripe-group}, and its buttons {@code stripe-button}, selected while their tool window shows.
 */
final class ToolWindowStripe extends VBox {

    /** The size of the icons of the buttons, in pixels. */
    static final double ICON_SIZE = 20;

    private final VBox top = new VBox();
    private final VBox bottom = new VBox();
    private final Map<ToolWindow, ButtonBase> buttons = new HashMap<>();

    ToolWindowStripe(ToolWindowAnchor anchor) {
        getStyleClass().addAll("stripe", "stripe-" + anchor.name().toLowerCase(Locale.ROOT));
        top.getStyleClass().add("stripe-group");
        bottom.getStyleClass().add("stripe-group");
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(top, spacer, bottom);
        BooleanBinding holdsButtons = Bindings.isNotEmpty(top.getChildren()).or(Bindings.isNotEmpty(bottom.getChildren()));
        visibleProperty().bind(holdsButtons);
        managedProperty().bind(holdsButtons);
    }

    /**
     * Adds the button of a tool window, after the buttons of its group: at the bottom for a tool window docked at the
     * bottom, at the top otherwise.
     *
     * @param toolWindow the tool window
     */
    void add(ToolWindow toolWindow) {
        ButtonBase button = ActionButtons.create(toolWindow.getToggleAction(), ICON_SIZE);
        button.getStyleClass().add("stripe-button");
        buttons.put(toolWindow, button);
        (toolWindow.getAnchor() == ToolWindowAnchor.BOTTOM ? bottom : top).getChildren().add(button);
    }

    /**
     * Removes the button of a tool window, the stripe hiding once it holds no button any more.
     *
     * @param toolWindow the tool window
     */
    void remove(ToolWindow toolWindow) {
        ButtonBase button = buttons.remove(toolWindow);
        top.getChildren().remove(button);
        bottom.getChildren().remove(button);
    }

    /**
     * Gives the buttons of the stripe, from the top to the bottom.
     *
     * @return the buttons
     */
    List<Node> getButtons() {
        return Stream.concat(top.getChildren().stream(), bottom.getChildren().stream()).toList();
    }
}
