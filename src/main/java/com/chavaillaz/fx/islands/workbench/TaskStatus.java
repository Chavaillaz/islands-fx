package com.chavaillaz.fx.islands.workbench;

import javafx.concurrent.Worker;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.util.Subscription;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

/**
 * A widget of the status bar telling how a task of the application stands: running, beside an indicator of its
 * progress, or failed, its details in a tooltip. It takes no room once the task is done, or before it starts.
 * <p>
 * It carries the style class {@code task-status}, and the pseudo-class {@code :running} or {@code :failed}. It follows
 * a {@link Worker} of JavaFX by itself, or is told how the task stands by the application.
 */
public final class TaskStatus extends Label {

    private static final PseudoClass RUNNING = PseudoClass.getPseudoClass("running");
    private static final PseudoClass FAILED = PseudoClass.getPseudoClass("failed");

    private final ProgressIndicator indicator = new ProgressIndicator();
    private final IconView failure = new IconView(Icons.ERROR);

    /**
     * Creates a widget of no task, taking no room.
     */
    public TaskStatus() {
        getStyleClass().add("task-status");
        setMnemonicParsing(false);
        managedProperty().bind(visibleProperty());
        done();
    }

    /**
     * Tells the task is running, its progress unknown.
     *
     * @param text what the task does, such as {@code Loading records…}
     */
    public void running(String text) {
        running(text, ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    /**
     * Tells the task is running.
     *
     * @param text     what the task does, such as {@code Loading records…}
     * @param progress how far it went, from 0 to 1, or a negative value when unknown
     */
    public void running(String text, double progress) {
        indicator.progressProperty().unbind();
        indicator.setProgress(progress);
        show(text, RUNNING, null);
    }

    /**
     * Tells the task failed.
     *
     * @param text    what failed, such as {@code Could not load the records}
     * @param details why, which the tooltip of the widget gives, or {@code null} for no tooltip
     */
    public void failed(String text, @Nullable String details) {
        show(text, FAILED, details);
    }

    /**
     * Tells the task is done, the widget taking no room.
     */
    public void done() {
        indicator.progressProperty().unbind();
        setVisible(false);
        setText("");
        setGraphic(null);
        setTooltip(null);
        pseudoClassStateChanged(RUNNING, false);
        pseudoClassStateChanged(FAILED, false);
    }

    /**
     * Tells how a worker of JavaFX stands, from now on: running while scheduled or running, under its message, or its
     * title while it has no message, failed with the message of its exception, done once succeeded or cancelled.
     *
     * @param worker the worker, such as a task or a service
     * @return the subscription, which stops following the worker once unsubscribed
     */
    public Subscription follow(Worker<?> worker) {
        Subscription subscription = worker.stateProperty().subscribe(state -> show(worker))
                .and(worker.messageProperty().subscribe(message -> show(worker)));
        return subscription.and(this::done);
    }

    private void show(Worker<?> worker) {
        String text = worker.getMessage() == null || worker.getMessage().isEmpty()
                ? worker.getTitle()
                : worker.getMessage();
        switch (worker.getState()) {
            case SCHEDULED, RUNNING -> {
                show(text, RUNNING, null);
                indicator.progressProperty().bind(worker.progressProperty());
            }
            case FAILED -> {
                Throwable exception = worker.getException();
                failed(text, exception == null ? null : exception.getMessage());
            }
            case READY, SUCCEEDED, CANCELLED -> done();
        }
    }

    private void show(String text, PseudoClass state, @Nullable String details) {
        setText(text);
        setGraphic(state == RUNNING ? indicator : failure);
        setTooltip(details == null ? null : new Tooltip(details));
        pseudoClassStateChanged(RUNNING, state == RUNNING);
        pseudoClassStateChanged(FAILED, state == FAILED);
        setVisible(true);
    }
}
