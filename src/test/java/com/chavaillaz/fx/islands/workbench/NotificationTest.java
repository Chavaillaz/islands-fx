package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import javafx.animation.Animation;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.util.Subscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.control.Tone;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Notifications")
class NotificationTest {

    private final Workbench workbench = new Workbench(false);
    private final NotificationLayer layer = workbench.getNotifications();

    @Test
    @DisplayName("Shows a notification of its tone, title, text and actions, until one is performed")
    void showsNotification() {
        SimpleBooleanProperty retried = new SimpleBooleanProperty();
        workbench.showNotification(Notification.builder(Tone.DANGER, "Could not load the records")
                .text("The server did not answer")
                .actions(Action.builder("test.retry", "Retry").handler(() -> retried.set(true)).build())
                .build());

        NotificationLayer.Balloon balloon = (NotificationLayer.Balloon) layer.getChildren().getFirst();
        assertThat(balloon.getStyleClass()).contains("notification");
        assertThat(balloon.getPseudoClassStates()).contains(PseudoClass.getPseudoClass("danger"));
        assertThat(balloon.getTimer()).isNull();
        assertThat(descendants(balloon).filter(IconView.class::isInstance).map(IconView.class::cast))
                .first().satisfies(icon -> assertThat(icon.getIcon()).isEqualTo(Icons.ERROR));
        assertThat(descendants(balloon).filter(Label.class::isInstance).map(node -> ((Label) node).getText()))
                .contains("Could not load the records", "The server did not answer");

        descendants(balloon).filter(Hyperlink.class::isInstance).map(Hyperlink.class::cast).findFirst()
                .orElseThrow().fire();
        assertThat(retried.get()).isTrue();
        assertThat(layer.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("Closes a notification from its button, or when the application unsubscribes")
    void closesNotification() {
        workbench.showNotification(Notification.builder(Tone.WARNING, "Disk almost full").build());
        Subscription saved = workbench.showNotification(Notification.builder(Tone.SUCCESS, "Saved").build());
        assertThat(layer.getChildren()).hasSize(2);

        saved.unsubscribe();
        assertThat(layer.getChildren()).hasSize(1);
        descendants((NotificationLayer.Balloon) layer.getChildren().getFirst())
                .filter(ButtonBase.class::isInstance).map(ButtonBase.class::cast).findFirst().orElseThrow().fire();
        assertThat(layer.getChildren()).isEmpty();
        assertThat(layer.isPickOnBounds()).isFalse();
    }

    @Test
    @DisplayName("Lets a notification go away by itself after its timeout, held back while the mouse rests on it")
    void goesAwayAfterTimeout() {
        workbench.showNotification(Notification.builder(Tone.INFO, "Records loaded").build());
        NotificationLayer.Balloon balloon = (NotificationLayer.Balloon) layer.getChildren().getFirst();
        Animation timer = balloon.getTimer();

        assertThat(timer).isNotNull();
        assertThat(timer.getTotalDuration()).isEqualTo(Notification.DEFAULT_TIMEOUT);
        assertThat(timer.getStatus()).isEqualTo(Animation.Status.RUNNING);

        timer.getOnFinished().handle(null);
        assertThat(layer.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("Keeps a warning or a danger until closed, unless given a timeout")
    void keepsWarningsUntilClosed() {
        assertThat(Notification.builder(Tone.WARNING, "Warning").build().getTimeout()).isEqualTo(Duration.INDEFINITE);
        assertThat(Notification.builder(Tone.DANGER, "Danger").timeout(Duration.seconds(3)).build().getTimeout())
                .isEqualTo(Duration.seconds(3));
        assertThat(Notification.builder(Tone.NEUTRAL, "Neutral").build().getText()).isEmpty();
    }

    private static Stream<Node> descendants(Parent parent) {
        return parent.getChildrenUnmodifiable().stream()
                .flatMap(child -> child instanceof Parent nested
                        ? Stream.concat(Stream.of(child), descendants(nested))
                        : Stream.of(child));
    }
}
