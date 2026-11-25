package com.chavaillaz.fx.islands.action;

/**
 * What menus and toolbars are made of: an {@link Action}, a {@link ActionGroup group} of them shown as a menu, or a
 * {@link ActionSeparator separator}.
 */
public sealed interface ActionElement permits Action, ActionGroup, ActionSeparator {
}
