package com.chavaillaz.fx.islands.action;

import java.util.List;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import com.chavaillaz.fx.islands.icon.IconView;

/**
 * Shows actions as menu items: the menus of a main menu, or the items of a context menu.
 * <p>
 * An item follows its action: it shows its text, icon and shortcut, is disabled along with it, and a toggle action is
 * shown as a check menu item reflecting whether the action is selected.
 */
public final class ActionMenus {

    private ActionMenus() {
    }

    /**
     * Creates a menu for each group, such as the menus of the main menu of a workbench.
     *
     * @param groups the groups, in the order of their menus
     * @return the menus
     */
    public static List<Menu> createMenus(List<ActionGroup> groups) {
        return groups.stream()
                .map(ActionMenus::createMenu)
                .toList();
    }

    /**
     * Creates a menu holding the elements of a group.
     *
     * @param group the group
     * @return the menu
     */
    public static Menu createMenu(ActionGroup group) {
        Menu menu = new Menu(group.text());
        menu.setMnemonicParsing(false);
        menu.getItems().setAll(createItems(group.elements()));
        return menu;
    }

    /**
     * Creates the items showing elements, a group being shown as a submenu.
     *
     * @param elements the elements, in the order of their items
     * @return the items
     */
    public static List<MenuItem> createItems(List<? extends ActionElement> elements) {
        return elements.stream()
                .map(ActionMenus::createItem)
                .toList();
    }

    /**
     * Creates the item showing an element.
     *
     * @param element the element
     * @return a menu for a group, a separator for a separator, and an item performing the action for an action
     */
    public static MenuItem createItem(ActionElement element) {
        return switch (element) {
            case Action action -> createActionItem(action);
            case ActionGroup group -> createMenu(group);
            case ActionSeparator _ -> new SeparatorMenuItem();
        };
    }

    private static MenuItem createActionItem(Action action) {
        MenuItem item;
        if (action.isToggle()) {
            CheckMenuItem checkItem = new CheckMenuItem(action.getText());
            action.selectedProperty().subscribe(checkItem::setSelected);
            // The item checks itself when chosen: it is set back to what the action toggled once performed
            checkItem.setOnAction(event -> {
                action.perform();
                checkItem.setSelected(action.isSelected());
            });
            item = checkItem;
        } else {
            item = new MenuItem(action.getText());
            item.setOnAction(event -> action.perform());
        }
        item.setMnemonicParsing(false);
        item.setAccelerator(action.getAccelerator());
        item.disableProperty().bind(action.disabledProperty());
        if (action.getIcon() != null) {
            item.setGraphic(new IconView(action.getIcon()));
        }
        return item;
    }
}
