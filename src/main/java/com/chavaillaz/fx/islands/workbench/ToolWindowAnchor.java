package com.chavaillaz.fx.islands.workbench;

/**
 * The side of the {@link Workbench} a {@link ToolWindow} docks on: beside the content, or under it.
 */
public enum ToolWindowAnchor {

    /** Docked on the left of the content, its button on the stripe along the left edge of the window. */
    LEFT,

    /** Docked on the right of the content, its button on the stripe along the right edge of the window. */
    RIGHT,

    /**
     * Docked under the content, between the tool windows docked on the sides, its button at the bottom of the stripe
     * along the left edge of the window. One tool window shows under the content at a time: showing one hides the one
     * showing there.
     */
    BOTTOM
}
