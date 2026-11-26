package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.css.PseudoClass;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Subscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("TaskStatus")
class TaskStatusTest {

    private static final PseudoClass RUNNING = PseudoClass.getPseudoClass("running");
    private static final PseudoClass FAILED = PseudoClass.getPseudoClass("failed");

    private final TaskStatus status = new TaskStatus();

    @Test
    @DisplayName("Tells a task running beside its progress, and takes no room once it is done")
    void tellsTaskRunning() {
        assertThat(status.isManaged()).isFalse();

        status.running("Loading records…");
        assertThat(status.isManaged()).isTrue();
        assertThat(status.getText()).isEqualTo("Loading records…");
        assertThat(status.getPseudoClassStates()).contains(RUNNING).doesNotContain(FAILED);
        assertThat(status.getGraphic()).isInstanceOfSatisfying(ProgressIndicator.class,
                indicator -> assertThat(indicator.isIndeterminate()).isTrue());

        status.running("Saving", 0.5);
        assertThat(((ProgressIndicator) status.getGraphic()).getProgress()).isEqualTo(0.5);

        status.done();
        assertThat(status.isManaged()).isFalse();
        assertThat(status.getPseudoClassStates()).doesNotContain(RUNNING, FAILED);
    }

    @Test
    @DisplayName("Tells a task failed, its details in its tooltip")
    void tellsTaskFailed() {
        status.failed("Could not load the records", "The server did not answer");

        assertThat(status.getPseudoClassStates()).contains(FAILED).doesNotContain(RUNNING);
        assertThat(status.getTooltip().getText()).isEqualTo("The server did not answer");
        assertThat(status.getGraphic()).isInstanceOfSatisfying(IconView.class,
                icon -> assertThat(icon.getIcon()).isEqualTo(Icons.ERROR));
    }

    @Test
    @DisplayName("Follows a worker of JavaFX, under its message while it runs, its title otherwise")
    void followsWorkerRunning() {
        DrivenWorker worker = new DrivenWorker();
        worker.title.set("Loading");
        status.follow(worker);
        assertThat(status.isVisible()).isFalse();

        worker.state.set(Worker.State.RUNNING);
        assertThat(status.getText()).isEqualTo("Loading");
        worker.message.set("Loading records…");
        worker.progress.set(0.4);
        assertThat(status.getText()).isEqualTo("Loading records…");
        assertThat(((ProgressIndicator) status.getGraphic()).getProgress()).isEqualTo(0.4);

        worker.state.set(Worker.State.SUCCEEDED);
        assertThat(status.isVisible()).isFalse();
    }

    @Test
    @DisplayName("Follows a worker of JavaFX failing, until unsubscribed")
    void followsWorkerFailing() {
        DrivenWorker worker = new DrivenWorker();
        worker.title.set("Saving the order");
        Subscription subscription = status.follow(worker);
        worker.exception.set(new IllegalStateException("The server did not answer"));
        worker.state.set(Worker.State.FAILED);

        assertThat(status.getPseudoClassStates()).contains(FAILED);
        assertThat(status.getText()).isEqualTo("Saving the order");
        assertThat(status.getTooltip().getText()).isEqualTo("The server did not answer");

        subscription.unsubscribe();
        assertThat(status.isVisible()).isFalse();
        worker.state.set(Worker.State.RUNNING);
        assertThat(status.isVisible()).isFalse();
    }

    /**
     * A worker going through the states the test sets, rather than those a task sets later on the JavaFX thread.
     */
    private static final class DrivenWorker implements Worker<Void> {

        private final ObjectProperty<State> state = new SimpleObjectProperty<>(this, "state", State.READY);
        private final ObjectProperty<Void> value = new SimpleObjectProperty<>(this, "value");
        private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<>(this, "exception");
        private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", -1);
        private final BooleanProperty running = new SimpleBooleanProperty(this, "running");
        private final StringProperty message = new SimpleStringProperty(this, "message", "");
        private final StringProperty title = new SimpleStringProperty(this, "title", "");

        @Override
        public State getState() {
            return state.get();
        }

        @Override
        public ReadOnlyObjectProperty<State> stateProperty() {
            return state;
        }

        @Override
        public Void getValue() {
            return value.get();
        }

        @Override
        public ReadOnlyObjectProperty<Void> valueProperty() {
            return value;
        }

        @Override
        public Throwable getException() {
            return exception.get();
        }

        @Override
        public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
            return exception;
        }

        @Override
        public double getWorkDone() {
            return -1;
        }

        @Override
        public ReadOnlyDoubleProperty workDoneProperty() {
            return progress;
        }

        @Override
        public double getTotalWork() {
            return -1;
        }

        @Override
        public ReadOnlyDoubleProperty totalWorkProperty() {
            return progress;
        }

        @Override
        public double getProgress() {
            return progress.get();
        }

        @Override
        public ReadOnlyDoubleProperty progressProperty() {
            return progress;
        }

        @Override
        public boolean isRunning() {
            return running.get();
        }

        @Override
        public ReadOnlyBooleanProperty runningProperty() {
            return running;
        }

        @Override
        public String getMessage() {
            return message.get();
        }

        @Override
        public ReadOnlyStringProperty messageProperty() {
            return message;
        }

        @Override
        public String getTitle() {
            return title.get();
        }

        @Override
        public ReadOnlyStringProperty titleProperty() {
            return title;
        }

        @Override
        public boolean cancel() {
            state.set(State.CANCELLED);
            return true;
        }
    }
}
