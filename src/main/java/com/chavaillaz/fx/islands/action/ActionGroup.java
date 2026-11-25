package com.chavaillaz.fx.islands.action;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 * Elements shown together and in order, as a menu: a menu of the menu bar, or a submenu within another group.
 *
 * @param text     the text of the menu showing the group
 * @param elements the actions, the groups shown as submenus and the separators of the group
 */
public record ActionGroup(String text, List<ActionElement> elements) implements ActionElement {

    /**
     * Creates a group, copying its elements.
     *
     * @param text     the text of the menu showing the group
     * @param elements the actions, the groups shown as submenus and the separators of the group
     */
    public ActionGroup {
        requireNonNull(text, "text");
        elements = List.copyOf(elements);
    }

    /**
     * Creates a group of the given elements.
     *
     * @param text     the text of the menu showing the group
     * @param elements the actions, the groups shown as submenus and the separators of the group
     * @return the group
     */
    public static ActionGroup of(String text, ActionElement... elements) {
        return new ActionGroup(text, List.of(elements));
    }
}
