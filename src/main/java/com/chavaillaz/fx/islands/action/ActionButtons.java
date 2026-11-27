package com.chavaillaz.fx.islands.action;

import java.util.List;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.IconView;

/**
 * Shows actions as toolbar buttons: borderless and showing the icon of the action, their background marking when
 * hovered or pressed, their tooltip giving the text and shortcut of the action.
 * <p>
 * The buttons carry the style class {@code action-button}. They do not take the keyboard focus, which stays where the
 * user works, the shortcuts of the actions standing for them.
 */
public final class ActionButtons {

    private ActionButtons() {
    }

    /**
     * Creates a button performing an action, showing its icon at the size of the grid of icons.
     *
     * @param action the action
     * @return a toggle button reflecting whether a toggle action is selected, a button otherwise
     */
    public static ButtonBase create(Action action) {
        return create(action, IconView.GRID);
    }

    /**
     * Creates a button performing an action.
     *
     * @param action   the action
     * @param iconSize the size of the icon in pixels
     * @return a toggle button reflecting whether a toggle action is selected, a button otherwise
     */
    public static ButtonBase create(Action action, double iconSize) {
        ButtonBase button;
        if (action.isToggle()) {
            ToggleButton toggleButton = new ToggleButton();
            action.selectedProperty().subscribe(toggleButton::setSelected);
            // The button toggles itself when fired: it is set back to what the action toggled once performed
            toggleButton.setOnAction(event -> {
                action.perform();
                toggleButton.setSelected(action.isSelected());
            });
            button = toggleButton;
        } else {
            button = new Button();
            button.setOnAction(event -> action.perform());
        }
        configure(button, action.getText(), action.getIcon(), action.tooltip(), iconSize);
        follow(button, action);
        button.disableProperty().bind(action.disabledProperty());
        return button;
    }

    /**
     * Makes a button follow the text of its action: its own text when it shows no icon, its tooltip, and the text
     * standing for it in the screen readers.
     */
    private static void follow(ButtonBase button, Action action) {
        if (action.getIcon() == null) {
            button.textProperty().bind(action.textProperty());
        }
        button.accessibleTextProperty().bind(action.textProperty());
        button.getTooltip().textProperty().bind(action.textProperty().map(text -> action.tooltip()));
    }

    /**
     * Creates a button opening a menu below it, such as the settings of the main toolbar.
     *
     * @param text     the text of the button, which its tooltip shows
     * @param icon     the icon of the button
     * @param elements the elements of the menu
     * @param iconSize the size of the icon in pixels
     * @return the button
     */
    public static Button createMenuButton(String text, Icon icon, List<? extends ActionElement> elements,
                                          double iconSize) {
        ContextMenu menu = new ContextMenu();
        menu.getItems().setAll(ActionMenus.createItems(elements));
        return createMenuButton(text, icon, menu, iconSize);
    }

    /**
     * Creates a button opening a menu below it, such as the main menu of the main toolbar.
     *
     * @param text     the text of the button, which its tooltip shows
     * @param icon     the icon of the button
     * @param menu     the menu, which the button opens
     * @param iconSize the size of the icon in pixels
     * @return the button
     */
    public static Button createMenuButton(String text, Icon icon, ContextMenu menu, double iconSize) {
        Button button = new Button();
        configure(button, text, icon, text, iconSize);
        // A click on the button while the menu shows only hides it, the menu consuming the clicks hiding it
        button.setOnAction(event -> menu.show(button, Side.BOTTOM, 0, 4));
        return button;
    }

    /**
     * Makes a button the application created, such as one an FXML file declares, look as the buttons of actions:
     * borderless, showing an icon, leaving the focus where the user works. Its text, if any, moves to its tooltip,
     * unless it has one, and stands for it in the screen readers.
     *
     * @param button the button
     * @param icon   the icon it shows
     */
    public static void decorate(ButtonBase button, Icon icon) {
        String text = button.getText();
        if (text != null && !text.isEmpty()) {
            if (button.getTooltip() == null) {
                button.setTooltip(new Tooltip(text));
            }
            if (button.getAccessibleText() == null) {
                button.setAccessibleText(text);
            }
            button.setText(null);
        }
        button.getStyleClass().setAll("action-button");
        button.setGraphic(new IconView(icon));
        button.setMnemonicParsing(false);
        button.setFocusTraversable(false);
    }

    private static void configure(ButtonBase button, String text, @Nullable Icon icon, String tooltip,
                                  double iconSize) {
        button.getStyleClass().setAll("action-button");
        if (icon != null) {
            button.setGraphic(new IconView(icon, iconSize));
        } else {
            button.setText(text);
        }
        button.setTooltip(new Tooltip(tooltip));
        button.setAccessibleText(text);
        button.setMnemonicParsing(false);
        button.setFocusTraversable(false);
    }
}
