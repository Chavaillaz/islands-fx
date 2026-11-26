package com.chavaillaz.fx.islands.workbench;

import java.lang.System.Logger.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Subscription;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.theme.ThemeManager;
import com.chavaillaz.fx.islands.theme.ThemeMode;

/**
 * Keeps how the user left the main window from one run to the next: its bounds, whether it was maximized, the theme
 * chosen, and for each tool window whether it showed and its size. The state is kept in a node of preferences, such as
 * the one of the user for the package of the application.
 * <p>
 * The state {@link #manage(Stage, Workbench, ThemeManager) restored} before the window shows is saved once it hides.
 * The bounds saved are those of the window neither maximized, iconified nor in full screen, which it gets back once
 * restored, and bounds standing on no screen any more are left out, the window then opening where the application
 * places it. A value that cannot be read, missing or damaged, leaves its part as the application made it.
 */
public final class WorkbenchState {

    private static final System.Logger LOGGER = System.getLogger(WorkbenchState.class.getName());

    private static final String THEME = "theme";
    private static final String WINDOW = "window.";
    private static final String TOOL_WINDOW = "toolWindow.";

    private final Preferences preferences;

    /**
     * Creates the state kept in a node of preferences.
     *
     * @param preferences the node, such as {@code Preferences.userNodeForPackage(Application.class)}
     */
    public WorkbenchState(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Restores the state of a window, then saves it once the window hides. It is called once the tool windows are
     * docked, before the window shows.
     *
     * @param stage     the window
     * @param workbench the workbench it shows, whose tool windows are restored
     * @param themes    the manager of the themes, whose choice is restored
     * @return the subscription, which stops saving the state once unsubscribed
     */
    public Subscription manage(Stage stage, Workbench workbench, ThemeManager themes) {
        restore(themes);
        restore(workbench);
        Tracker tracker = new Tracker(stage);
        tracker.restore();
        EventHandler<WindowEvent> onHidden = event -> {
            save(themes);
            save(workbench);
            tracker.save();
            flush();
        };
        stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, onHidden);
        return tracker.subscription.and(() -> stage.removeEventHandler(WindowEvent.WINDOW_HIDDEN, onHidden));
    }

    /**
     * Restores the theme the user chose, unless none was saved.
     *
     * @param themes the manager of the themes
     */
    public void restore(ThemeManager themes) {
        String name = preferences.get(THEME, null);
        for (ThemeMode mode : ThemeMode.values()) {
            if (mode.name().equals(name)) {
                themes.setMode(mode);
            }
        }
    }

    /**
     * Restores whether each tool window showed and its size, for those saved.
     *
     * @param workbench the workbench, its tool windows docked
     */
    public void restore(Workbench workbench) {
        for (ToolWindow toolWindow : workbench.getToolWindows()) {
            String key = TOOL_WINDOW + toolWindow.getId() + '.';
            double width = preferences.getDouble(key + "width", Double.NaN);
            double height = preferences.getDouble(key + "height", Double.NaN);
            if (width > 0) {
                toolWindow.setWidth(width);
            }
            if (height > 0) {
                toolWindow.setHeight(height);
            }
            if (preferences.get(key + "visible", null) != null) {
                toolWindow.setVisible(preferences.getBoolean(key + "visible", true));
            }
        }
    }

    /**
     * Saves the theme the user chose.
     *
     * @param themes the manager of the themes
     */
    public void save(ThemeManager themes) {
        preferences.put(THEME, themes.getMode().name());
    }

    /**
     * Saves whether each tool window shows and its size.
     *
     * @param workbench the workbench
     */
    public void save(Workbench workbench) {
        for (ToolWindow toolWindow : workbench.getToolWindows()) {
            String key = TOOL_WINDOW + toolWindow.getId() + '.';
            preferences.putBoolean(key + "visible", toolWindow.isVisible());
            preferences.putDouble(key + "width", toolWindow.getWidth());
            preferences.putDouble(key + "height", toolWindow.getHeight());
        }
    }

    /**
     * Writes the state saved to the store of the preferences, logging rather than failing when it cannot, the state
     * being worth less than the work of the user.
     */
    public void flush() {
        try {
            preferences.flush();
        } catch (BackingStoreException exception) {
            LOGGER.log(Level.WARNING, "Could not save the state of the window", exception);
        }
    }

    /**
     * Follows the bounds of a window while neither maximized, iconified nor in full screen, the ones it gets back
     * once restored, and those saved.
     */
    private final class Tracker {

        private final Stage stage;
        private final Subscription subscription;
        private @Nullable Rectangle2D normal;

        private Tracker(Stage stage) {
            this.stage = stage;
            InvalidationListener listener = observable -> remember();
            stage.xProperty().addListener(listener);
            stage.yProperty().addListener(listener);
            stage.widthProperty().addListener(listener);
            stage.heightProperty().addListener(listener);
            subscription = () -> {
                stage.xProperty().removeListener(listener);
                stage.yProperty().removeListener(listener);
                stage.widthProperty().removeListener(listener);
                stage.heightProperty().removeListener(listener);
            };
        }

        /**
         * Places the window where it was saved, unless no screen shows that place any more.
         */
        private void restore() {
            double x = preferences.getDouble(WINDOW + "x", Double.NaN);
            double y = preferences.getDouble(WINDOW + "y", Double.NaN);
            double width = preferences.getDouble(WINDOW + "width", Double.NaN);
            double height = preferences.getDouble(WINDOW + "height", Double.NaN);
            if (Double.isNaN(x) || Double.isNaN(y) || !(width > 0) || !(height > 0)
                    || Screen.getScreensForRectangle(x, y, width, height).isEmpty()) {
                return;
            }
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(width);
            stage.setHeight(height);
            normal = new Rectangle2D(x, y, width, height);
            stage.setMaximized(preferences.getBoolean(WINDOW + "maximized", false));
        }

        private void remember() {
            if (!stage.isMaximized() && !stage.isIconified() && !stage.isFullScreen()) {
                normal = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            }
        }

        private void save() {
            Rectangle2D bounds = normal != null
                    ? normal
                    : new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            preferences.putDouble(WINDOW + "x", bounds.getMinX());
            preferences.putDouble(WINDOW + "y", bounds.getMinY());
            preferences.putDouble(WINDOW + "width", bounds.getWidth());
            preferences.putDouble(WINDOW + "height", bounds.getHeight());
            preferences.putBoolean(WINDOW + "maximized", stage.isMaximized());
        }
    }
}
