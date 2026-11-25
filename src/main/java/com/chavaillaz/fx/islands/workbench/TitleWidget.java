package com.chavaillaz.fx.islands.workbench;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderDragType;

/**
 * The title of the window in the main toolbar: a badge of the initials of the title, then the title. Dragging it
 * moves the window, as dragging a title bar does.
 * <p>
 * It carries the style class {@code title-widget}, its badge {@code title-badge} and its title {@code title-name}.
 */
public final class TitleWidget extends HBox {

    /**
     * Creates the widget of a title.
     *
     * @param title the title, such as the name of the application
     */
    public TitleWidget(String title) {
        Label badge = new Label(initials(title));
        badge.getStyleClass().add("title-badge");
        Label name = new Label(title);
        name.getStyleClass().add("title-name");
        getChildren().addAll(badge, name);
        getStyleClass().add("title-widget");
        HeaderBar.setDragType(this, HeaderDragType.DRAGGABLE_SUBTREE);
    }

    /**
     * Gives the initials of a title, as its badge shows them.
     *
     * @param title the title
     * @return the first letters of its first two words, or the first two letters of its single word, in uppercase
     */
    static String initials(String title) {
        String[] words = title.strip().split("\\s+");
        String initials = words.length > 1
                ? Arrays.stream(words).limit(2).map(word -> word.substring(0, 1)).collect(Collectors.joining())
                : words[0].substring(0, Math.min(2, words[0].length()));
        return initials.toUpperCase(Locale.ROOT);
    }
}
