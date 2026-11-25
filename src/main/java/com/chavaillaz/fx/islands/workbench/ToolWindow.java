package com.chavaillaz.fx.islands.workbench;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.Icons;

/**
 * A panel docked on a side of the {@link Workbench}, in an island of its own, which a button of the stripe on that
 * side shows and hides.
 * <p>
 * Its header shows its title, the buttons of its actions and a button hiding it. It keeps its width while the window
 * is resized, the content taking what is left, and the user resizes it by dragging the gap beside it.
 */
public final class ToolWindow {

    /** The width of a tool window unless given another one, in pixels. */
    public static final double DEFAULT_WIDTH = 600;

    /** The narrowest a tool window gets while the window has the room, in pixels. */
    public static final double MIN_WIDTH = 160;

    private final String id;
    private final String title;
    private final Icon icon;
    private final ToolWindowAnchor anchor;
    private final Node content;
    private final List<Action> actions;
    private final BooleanProperty visible = new SimpleBooleanProperty(this, "visible", true);
    private final DoubleProperty width;
    private final Action toggleAction;
    private final Action hideAction;

    private ToolWindow(Builder builder) {
        id = builder.id;
        title = builder.title;
        icon = builder.icon;
        anchor = builder.anchor;
        content = builder.content;
        actions = builder.actions;
        width = new SimpleDoubleProperty(this, "width", builder.width);
        Action.Builder toggle = Action.builder(id + ".toggle", title)
                .description("Shows or hides the " + title + " tool window")
                .icon(icon)
                .selected(visible)
                .handler(() -> setVisible(!isVisible()));
        if (builder.accelerator != null) {
            toggle.accelerator(builder.accelerator);
        }
        toggleAction = toggle.build();
        hideAction = Action.builder(id + ".hide", "Hide")
                .icon(Icons.HIDE)
                .handler(() -> setVisible(false))
                .build();
    }

    /**
     * Starts describing a tool window.
     *
     * @param id      the identifier of the tool window, unique in the workbench, which its state is saved under
     * @param title   the title of the tool window, shown in its header and the tooltip of its button
     * @param icon    the icon of its button
     * @param content what the tool window shows under its header
     * @return the builder of the tool window
     */
    public static Builder builder(String id, String title, Icon icon, Node content) {
        return new Builder(id, title, icon, content);
    }

    /**
     * Gives the identifier of the tool window.
     *
     * @return the identifier, unique in the workbench
     */
    public String getId() {
        return id;
    }

    /**
     * Gives the title of the tool window.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gives the icon of the button of the tool window.
     *
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Gives the side of the workbench the tool window docks on.
     *
     * @return the side
     */
    public ToolWindowAnchor getAnchor() {
        return anchor;
    }

    /**
     * Gives what the tool window shows under its header.
     *
     * @return the content
     */
    public Node getContent() {
        return content;
    }

    /**
     * Gives the actions whose buttons the header shows, before the one hiding the tool window.
     *
     * @return the actions, in the order of their buttons
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Gives the action showing the tool window when hidden and hiding it when shown, which its stripe button and
     * menu item perform.
     *
     * @return the toggle action, selected while the tool window shows
     */
    public Action getToggleAction() {
        return toggleAction;
    }

    /**
     * Gives the action hiding the tool window, which the last button of its header performs.
     *
     * @return the action
     */
    public Action getHideAction() {
        return hideAction;
    }

    /**
     * Gives whether the tool window shows.
     *
     * @return the property of the visibility
     */
    public BooleanProperty visibleProperty() {
        return visible;
    }

    /**
     * Tells whether the tool window shows.
     *
     * @return {@code true} when it shows
     */
    public boolean isVisible() {
        return visible.get();
    }

    /**
     * Shows or hides the tool window.
     *
     * @param visible {@code true} to show it
     */
    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    /**
     * Gives the width the tool window takes while the window has the room, in pixels.
     *
     * @return the property of the width, which dragging the gap beside the tool window changes
     */
    public DoubleProperty widthProperty() {
        return width;
    }

    /**
     * Gives the width the tool window takes while the window has the room.
     *
     * @return the width in pixels
     */
    public double getWidth() {
        return width.get();
    }

    /**
     * Changes the width the tool window takes while the window has the room.
     *
     * @param width the width in pixels
     */
    public void setWidth(double width) {
        this.width.set(width);
    }

    /**
     * Describes a tool window, step by step, before {@link #build() building} it.
     */
    public static final class Builder {

        private final String id;
        private final String title;
        private final Icon icon;
        private final Node content;
        private ToolWindowAnchor anchor = ToolWindowAnchor.LEFT;
        private double width = DEFAULT_WIDTH;
        private @Nullable KeyCombination accelerator;
        private List<Action> actions = List.of();

        private Builder(String id, String title, Icon icon, Node content) {
            this.id = requireNonNull(id, "id");
            this.title = requireNonNull(title, "title");
            this.icon = requireNonNull(icon, "icon");
            this.content = requireNonNull(content, "content");
        }

        /**
         * Docks the tool window on a side, the left one unless told otherwise.
         *
         * @param anchor the side
         * @return this builder
         */
        public Builder anchor(ToolWindowAnchor anchor) {
            this.anchor = requireNonNull(anchor, "anchor");
            return this;
        }

        /**
         * Gives the tool window its width, {@link #DEFAULT_WIDTH} unless told otherwise.
         *
         * @param width the width in pixels
         * @return this builder
         */
        public Builder width(double width) {
            this.width = width;
            return this;
        }

        /**
         * Gives a shortcut to the action showing and hiding the tool window.
         *
         * @param accelerator the combination of keys, such as {@code Shortcut+1}
         * @return this builder
         */
        public Builder accelerator(KeyCombination accelerator) {
            this.accelerator = accelerator;
            return this;
        }

        /**
         * Gives the actions whose buttons the header of the tool window shows.
         *
         * @param actions the actions, in the order of their buttons
         * @return this builder
         */
        public Builder actions(Action... actions) {
            this.actions = List.of(actions);
            return this;
        }

        /**
         * Builds the tool window described, showing.
         *
         * @return the tool window
         */
        public ToolWindow build() {
            return new ToolWindow(this);
        }
    }
}
