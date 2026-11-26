package com.chavaillaz.fx.islands.theme;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.DialogPane;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

/**
 * Gives the alerts of JavaFX the icons of the library, outlines colored by the theme, rather than the images Modena
 * gives them: an information, a warning, an error, or a question for a confirmation and the dialogs asking for a value.
 * <p>
 * An alert keeps the graphic its application gave it. The icon carries the style class {@code dialog-icon}, and follows
 * the type of the alert when it changes.
 */
final class DialogIcons {

    /** The size of the icons, in pixels. */
    static final double SIZE = 32;

    /** The key marking, in the properties of a node, the icon given by this class. */
    private static final String ICON_KEY = DialogIcons.class.getName() + ".icon";

    /** The key marking, in the properties of a dialog pane, the graphic this class manages. */
    private static final String MANAGED_KEY = DialogIcons.class.getName() + ".managed";

    private DialogIcons() {
    }

    /**
     * Gives a dialog pane the icon of its type, unless its application gave it a graphic of its own, then follows the
     * changes of its type: an alert changing its type first loses the style class of the former one.
     *
     * @param pane the dialog pane, the root of the scene of a dialog
     */
    static void decorate(DialogPane pane) {
        Node graphic = pane.getGraphic();
        boolean managed = pane.getProperties().containsKey(MANAGED_KEY);
        boolean ours = graphic == null ? managed : graphic.getProperties().containsKey(ICON_KEY);
        StyleOrigin origin = ((StyleableProperty<?>) pane.graphicProperty()).getStyleOrigin();
        if (origin == StyleOrigin.USER && !ours) {
            return;
        }
        Icon icon = icon(pane.getStyleClass());
        if (icon == null) {
            if (graphic != null && ours) {
                pane.setGraphic(null);
            }
            return;
        }
        if (!(graphic instanceof IconView shown && shown.getIcon() == icon)) {
            IconView view = new IconView(icon, SIZE);
            view.getStyleClass().add("dialog-icon");
            view.getProperties().put(ICON_KEY, Boolean.TRUE);
            pane.setGraphic(view);
        }
        if (!managed) {
            pane.getProperties().put(MANAGED_KEY, Boolean.TRUE);
            pane.getStyleClass().addListener((ListChangeListener<String>) change -> decorate(pane));
        }
    }

    /**
     * Gives the icon of a dialog pane after its style classes, which the type of an alert sets.
     */
    private static @Nullable Icon icon(List<String> styleClasses) {
        if (styleClasses.contains("text-input-dialog") || styleClasses.contains("choice-dialog")) {
            return Icons.HELP;
        }
        if (!styleClasses.contains("alert")) {
            return null;
        }
        if (styleClasses.contains("information")) {
            return Icons.INFO;
        } else if (styleClasses.contains("warning")) {
            return Icons.WARNING;
        } else if (styleClasses.contains("error")) {
            return Icons.ERROR;
        } else if (styleClasses.contains("confirmation")) {
            return Icons.HELP;
        }
        return null;
    }
}
