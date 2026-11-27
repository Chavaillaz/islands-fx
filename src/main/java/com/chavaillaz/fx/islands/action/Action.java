package com.chavaillaz.fx.islands.action;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.input.KeyCombination;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.icon.Icon;

/**
 * Something the user can do, described once and shown the same way wherever it stands: as a menu item, a toolbar
 * button or a tool window button (see {@link ActionMenus} and {@link ActionButtons}), each showing its text, icon and
 * shortcut, and following whether it is enabled.
 * <p>
 * An action does not hold the state it shows: it follows the observable values it is given, which its handler
 * changes. A toggle action, given whether it is {@link Builder#selected(ObservableBooleanValue) selected}, is shown as
 * a check menu item or a toggle button reflecting that value, which choosing it does not change by itself, but
 * through the handler. Its text can follow a value as well, such as the title of a tool window the application renames
 * ({@link Builder#text(ObservableStringValue)}), which the menu items, buttons and links showing it follow too.
 */
public final class Action implements ActionElement {

    private final String id;
    private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper(this, "text");
    private final ReadOnlyStringWrapper description = new ReadOnlyStringWrapper(this, "description");
    private final @Nullable Icon icon;
    private final @Nullable KeyCombination accelerator;
    private final boolean toggle;
    private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper(this, "selected");
    private final ReadOnlyBooleanWrapper disabled = new ReadOnlyBooleanWrapper(this, "disabled");
    private final Runnable handler;

    private Action(Builder builder) {
        id = builder.id;
        text.set(builder.text);
        if (builder.followedText != null) {
            text.bind(builder.followedText);
        }
        description.set(builder.description);
        if (builder.followedDescription != null) {
            description.bind(builder.followedDescription);
        }
        icon = builder.icon;
        accelerator = builder.accelerator;
        toggle = builder.selected != null;
        handler = requireNonNull(builder.handler, () -> "The action " + id + " has no handler");
        if (builder.selected != null) {
            selected.bind(builder.selected);
        }
        if (builder.disabled != null) {
            disabled.bind(builder.disabled);
        }
    }

    /**
     * Starts describing an action.
     *
     * @param id   the identifier of the action, unique in the application, such as {@code records.refresh}
     * @param text the text of the action, as menus show it
     * @return the builder of the action
     */
    public static Builder builder(String id, String text) {
        return new Builder(id, text);
    }

    /**
     * Gives the identifier of the action.
     *
     * @return the identifier, unique in the application
     */
    public String getId() {
        return id;
    }

    /**
     * Gives the text of the action, as menus show it.
     *
     * @return the text
     */
    public String getText() {
        return requireNonNullElse(text.get(), "");
    }

    /**
     * Gives the text of the action, which changes when it follows a value.
     *
     * @return the property following the text the action was given
     */
    public ReadOnlyStringProperty textProperty() {
        return text.getReadOnlyProperty();
    }

    /**
     * Gives what the action does, in a sentence its tooltip adds to its text.
     *
     * @return the description, or {@code null} for an action its text describes well enough
     */
    public @Nullable String getDescription() {
        String value = description.get();
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     * Gives what the action does, which changes when it follows a value.
     *
     * @return the property following the description the action was given, empty or {@code null} for none
     */
    public ReadOnlyStringProperty descriptionProperty() {
        return description.getReadOnlyProperty();
    }

    /**
     * Gives the icon of the action, which toolbar buttons show instead of its text.
     *
     * @return the icon, or {@code null} for none
     */
    public @Nullable Icon getIcon() {
        return icon;
    }

    /**
     * Gives the shortcut performing the action.
     *
     * @return the combination of keys, or {@code null} for none
     */
    public @Nullable KeyCombination getAccelerator() {
        return accelerator;
    }

    /**
     * Tells whether the action toggles a state, which it is then shown as selected or not.
     *
     * @return {@code true} for a toggle action
     */
    public boolean isToggle() {
        return toggle;
    }

    /**
     * Gives whether the state a toggle action toggles is on, which is always {@code false} for another action.
     *
     * @return the property following the value the action was given
     */
    public ReadOnlyBooleanProperty selectedProperty() {
        return selected.getReadOnlyProperty();
    }

    /**
     * Tells whether the state a toggle action toggles is on.
     *
     * @return {@code true} when on, always {@code false} for an action toggling nothing
     */
    public boolean isSelected() {
        return selected.get();
    }

    /**
     * Gives whether the action cannot be performed at the moment, which disables what shows it.
     *
     * @return the property following the value the action was given
     */
    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled.getReadOnlyProperty();
    }

    /**
     * Tells whether the action cannot be performed at the moment.
     *
     * @return {@code true} when disabled
     */
    public boolean isDisabled() {
        return disabled.get();
    }

    /**
     * Performs the action, unless it is disabled.
     */
    public void perform() {
        if (!isDisabled()) {
            handler.run();
        }
    }

    /**
     * Gives the text of a tooltip presenting the action: its text and shortcut, then its description.
     *
     * @return the text of the tooltip
     */
    public String tooltip() {
        StringBuilder tooltip = new StringBuilder(getText());
        if (accelerator != null) {
            tooltip.append("  ").append(accelerator.getDisplayText());
        }
        String about = getDescription();
        if (about != null) {
            tooltip.append('\n').append(about);
        }
        return tooltip.toString();
    }

    @Override
    public String toString() {
        return "Action[" + id + "]";
    }

    /**
     * Describes an action, step by step, before {@link #build() building} it.
     */
    public static final class Builder {

        private final String id;
        private final String text;
        private @Nullable ObservableStringValue followedText;
        private @Nullable String description;
        private @Nullable ObservableStringValue followedDescription;
        private @Nullable Icon icon;
        private @Nullable KeyCombination accelerator;
        private @Nullable ObservableBooleanValue selected;
        private @Nullable ObservableBooleanValue disabled;
        private @Nullable Runnable handler;

        private Builder(String id, String text) {
            this.id = requireNonNull(id, "id");
            this.text = requireNonNull(text, "text");
        }

        /**
         * Makes the text of the action follow a value instead of the one given first, such as the title of a tool
         * window the application renames: the menu items, buttons and links showing the action follow it as well.
         *
         * @param text the text of the action, as menus show it
         * @return this builder
         */
        public Builder text(ObservableStringValue text) {
            this.followedText = requireNonNull(text, "text");
            return this;
        }

        /**
         * Gives the action a description, which its tooltip adds to its text.
         *
         * @param description what the action does, in a sentence
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Gives the action a description following a value, such as one naming the title of a tool window renamed.
         *
         * @param description what the action does, in a sentence
         * @return this builder
         */
        public Builder description(ObservableStringValue description) {
            this.followedDescription = requireNonNull(description, "description");
            return this;
        }

        /**
         * Gives the action an icon, which toolbar buttons show instead of its text.
         *
         * @param icon the icon
         * @return this builder
         */
        public Builder icon(Icon icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Gives the action a shortcut, which performs it from anywhere in the window.
         *
         * @param accelerator the combination of keys, such as {@code Shortcut+R}
         * @return this builder
         */
        public Builder accelerator(KeyCombination accelerator) {
            this.accelerator = accelerator;
            return this;
        }

        /**
         * Makes the action a toggle action, shown as selected while the given value is {@code true}.
         *
         * @param selected whether the state the action toggles is on, which the handler changes
         * @return this builder
         */
        public Builder selected(ObservableBooleanValue selected) {
            this.selected = selected;
            return this;
        }

        /**
         * Disables the action while the given value is {@code true}.
         *
         * @param disabled whether the action cannot be performed
         * @return this builder
         */
        public Builder disabled(ObservableBooleanValue disabled) {
            this.disabled = disabled;
            return this;
        }

        /**
         * Gives the action what it does, which every action needs.
         *
         * @param handler what the action does, run on the JavaFX application thread
         * @return this builder
         */
        public Builder handler(Runnable handler) {
            this.handler = handler;
            return this;
        }

        /**
         * Builds the action described.
         *
         * @return the action
         * @throws NullPointerException when no handler was given
         */
        public Action build() {
            return new Action(this);
        }
    }
}
