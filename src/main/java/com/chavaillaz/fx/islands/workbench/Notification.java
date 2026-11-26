package com.chavaillaz.fx.islands.workbench;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.util.Duration;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.control.Tone;

/**
 * A message the {@link Workbench} shows in a balloon over the bottom right corner of the window, above the status bar:
 * an icon and a title after its {@link Tone}, a text, and links to the actions it offers, such as retrying what failed.
 * <p>
 * A notification goes away after its timeout, which the mouse resting on it holds back, or once closed by the user, by
 * one of its actions, or by the application. A warning or a danger stays until closed, unless given a timeout.
 */
public final class Notification {

    /** The time a notification shows unless told otherwise, but for a warning or a danger. */
    public static final Duration DEFAULT_TIMEOUT = Duration.seconds(8);

    private final Tone tone;
    private final String title;
    private final String text;
    private final List<Action> actions;
    private final Duration timeout;

    private Notification(Builder builder) {
        tone = builder.tone;
        title = builder.title;
        text = builder.text;
        actions = builder.actions;
        timeout = builder.timeout;
    }

    /**
     * Starts describing a notification.
     *
     * @param tone  the tone, telling at a glance how things stand
     * @param title the title, in a few words
     * @return the builder of the notification
     */
    public static Builder builder(Tone tone, String title) {
        return new Builder(tone, title);
    }

    /**
     * Gives the tone of the notification, which its icon shows.
     *
     * @return the tone
     */
    public Tone getTone() {
        return tone;
    }

    /**
     * Gives the title of the notification.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gives the text under the title.
     *
     * @return the text, empty for none
     */
    public String getText() {
        return text;
    }

    /**
     * Gives the actions the notification offers, shown as links, each closing it once performed.
     *
     * @return the actions, in the order of their links
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Gives the time the notification shows before going away by itself.
     *
     * @return the time, {@link Duration#INDEFINITE} for a notification staying until closed
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Describes a notification, step by step, before {@link #build() building} it.
     */
    public static final class Builder {

        private final Tone tone;
        private final String title;
        private String text = "";
        private List<Action> actions = List.of();
        private Duration timeout;

        private Builder(Tone tone, String title) {
            this.tone = requireNonNull(tone, "tone");
            this.title = requireNonNull(title, "title");
            timeout = tone == Tone.WARNING || tone == Tone.DANGER ? Duration.INDEFINITE : DEFAULT_TIMEOUT;
        }

        /**
         * Gives the notification a text under its title.
         *
         * @param text the text, telling more
         * @return this builder
         */
        public Builder text(String text) {
            this.text = requireNonNull(text, "text");
            return this;
        }

        /**
         * Gives the notification actions, shown as links under its text.
         *
         * @param actions the actions, in the order of their links
         * @return this builder
         */
        public Builder actions(Action... actions) {
            this.actions = List.of(actions);
            return this;
        }

        /**
         * Gives the time the notification shows before going away by itself, {@link #DEFAULT_TIMEOUT} unless told
         * otherwise, or until closed for a warning or a danger.
         *
         * @param timeout the time, {@link Duration#INDEFINITE} to stay until closed
         * @return this builder
         */
        public Builder timeout(Duration timeout) {
            this.timeout = requireNonNull(timeout, "timeout");
            return this;
        }

        /**
         * Builds the notification described.
         *
         * @return the notification
         */
        public Notification build() {
            return new Notification(this);
        }
    }
}
