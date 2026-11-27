package com.chavaillaz.fx.islands.workbench;

import java.util.Map;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.Subscription;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.control.Tone;
import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.internal.Texts;

/**
 * The layer over the workbench showing its notifications, stacked in the bottom right corner of the window, the latest
 * at the bottom. It lets the mouse through where it shows none.
 * <p>
 * It carries the style class {@code notification-layer}. A notification carries the style class {@code notification}
 * and the pseudo-class of its tone, its icon {@code notification-icon}, its title {@code notification-title}, its text
 * {@code notification-text}, and its links {@code notification-action}.
 */
final class NotificationLayer extends VBox {

    /** The width of a notification, in pixels. */
    static final double WIDTH = 360;

    private static final Map<Tone, Icon> ICONS = Map.of(
            Tone.NEUTRAL, Icons.INFO,
            Tone.INFO, Icons.INFO,
            Tone.SUCCESS, Icons.CHECK,
            Tone.WARNING, Icons.WARNING,
            Tone.DANGER, Icons.ERROR);

    NotificationLayer() {
        getStyleClass().add("notification-layer");
        setAlignment(Pos.BOTTOM_RIGHT);
        setFillWidth(false);
        setPickOnBounds(false);
    }

    /**
     * Shows a notification, under those showing.
     *
     * @param notification the notification
     * @return the subscription, which closes the notification once unsubscribed
     */
    Subscription show(Notification notification) {
        Balloon balloon = new Balloon(notification);
        getChildren().add(balloon);
        balloon.start();
        return balloon::close;
    }

    /**
     * The balloon showing a notification, closing itself after its timeout while the mouse does not rest on it.
     */
    final class Balloon extends VBox {

        private final @Nullable PauseTransition timer;

        private Balloon(Notification notification) {
            getStyleClass().add("notification");
            pseudoClassStateChanged(notification.getTone().pseudoClass(), true);
            setPrefWidth(WIDTH);
            setMaxWidth(WIDTH);

            IconView icon = new IconView(ICONS.get(notification.getTone()));
            icon.getStyleClass().add("notification-icon");
            Label title = new Label(notification.getTitle());
            title.getStyleClass().add("notification-title");
            title.setWrapText(true);
            title.setMnemonicParsing(false);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button close = new Button(Texts.get("notification.close"));
            ActionButtons.decorate(close, Icons.CLOSE);
            close.setOnAction(event -> close());
            HBox header = new HBox(8, icon, title, spacer, close);
            header.setAlignment(Pos.TOP_LEFT);
            getChildren().add(header);

            if (!notification.getText().isEmpty()) {
                Label text = new Label(notification.getText());
                text.getStyleClass().add("notification-text");
                text.setWrapText(true);
                text.setMnemonicParsing(false);
                getChildren().add(text);
            }
            if (!notification.getActions().isEmpty()) {
                FlowPane links = new FlowPane();
                links.getStyleClass().add("notification-actions");
                notification.getActions().forEach(action -> links.getChildren().add(link(action)));
                getChildren().add(links);
            }

            Duration timeout = notification.getTimeout();
            timer = timeout.isIndefinite() ? null : new PauseTransition(timeout);
            if (timer != null) {
                timer.setOnFinished(event -> close());
                hoverProperty().subscribe((wasHovered, hovered) -> {
                    if (hovered) {
                        timer.stop();
                    } else {
                        timer.playFromStart();
                    }
                });
            }
        }

        /**
         * Gives the timer closing the balloon, for the tests to finish it.
         *
         * @return the timer, or {@code null} for a notification staying until closed
         */
        @Nullable PauseTransition getTimer() {
            return timer;
        }

        private void start() {
            if (timer != null) {
                timer.playFromStart();
            }
        }

        private void close() {
            if (timer != null) {
                timer.stop();
            }
            NotificationLayer.this.getChildren().remove(this);
        }

        private ButtonBase link(Action action) {
            Hyperlink link = new Hyperlink();
            link.textProperty().bind(action.textProperty());
            link.getStyleClass().add("notification-action");
            link.setMnemonicParsing(false);
            link.disableProperty().bind(action.disabledProperty());
            link.setOnAction(event -> {
                close();
                action.perform();
            });
            return link;
        }
    }
}
