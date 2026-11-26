package com.chavaillaz.fx.islands.control;

import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A text field searching something, such as the rows of a table: a magnifier before its text, and after it a button
 * clearing it once filled, as the escape key does.
 * <p>
 * It carries the style classes {@code text-field} and {@code search-field}, its magnifier {@code search-field-icon} and
 * its button {@code search-field-clear}. The escape key, once the field is empty, goes on to the window, such as
 * closing a dialog.
 */
public final class SearchField extends TextField {

    /**
     * Creates an empty search field.
     */
    public SearchField() {
        getStyleClass().add("search-field");
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && !getText().isEmpty()) {
                clear();
                event.consume();
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SearchFieldSkin(this);
    }
}
