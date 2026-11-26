package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.prefs.Preferences;

import javafx.application.ColorScheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Subscription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.Icons;
import com.chavaillaz.fx.islands.theme.ThemeManager;
import com.chavaillaz.fx.islands.theme.ThemeMode;

@ExtendWith(JavaFxExtension.class)
@DisplayName("WorkbenchState")
class WorkbenchStateTest {

    private final Preferences preferences = new MemoryPreferences();
    private final ThemeManager themes = new ThemeManager(ThemeMode.SYSTEM, new SimpleObjectProperty<>(ColorScheme.LIGHT));
    private final Workbench workbench = new Workbench(false);
    private final ToolWindow records = ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane()).build();
    private final ToolWindow log = ToolWindow.builder("log", "Log", Icons.LIST, new StackPane())
            .anchor(ToolWindowAnchor.BOTTOM)
            .build();
    private final Stage stage = new Stage();

    WorkbenchStateTest() {
        workbench.addToolWindow(records);
        workbench.addToolWindow(log);
        workbench.createScene(stage);
    }

    @AfterEach
    void hideStage() {
        stage.hide();
    }

    @Test
    @DisplayName("Restores the theme, the tool windows and the bounds of the window saved")
    void restoresStateSaved() {
        preferences.put("theme", "DARK");
        preferences.putBoolean("toolWindow.records.visible", false);
        preferences.putDouble("toolWindow.records.width", 420);
        preferences.putDouble("toolWindow.log.height", 180);
        preferences.putDouble("window.x", 100);
        preferences.putDouble("window.y", 80);
        preferences.putDouble("window.width", 900);
        preferences.putDouble("window.height", 600);
        new WorkbenchState(preferences).manage(stage, workbench, themes);

        assertThat(themes.getMode()).isEqualTo(ThemeMode.DARK);
        assertThat(records.isVisible()).isFalse();
        assertThat(records.getWidth()).isEqualTo(420);
        assertThat(log.isVisible()).isTrue();
        assertThat(log.getHeight()).isEqualTo(180);
        assertThat(stage.getX()).isEqualTo(100);
        assertThat(stage.getWidth()).isEqualTo(900);
        assertThat(stage.isMaximized()).isFalse();
    }

    @Test
    @DisplayName("Saves the state once the window hides")
    void savesStateOnHide() {
        new WorkbenchState(preferences).manage(stage, workbench, themes);
        themes.setMode(ThemeMode.LIGHT);
        log.setVisible(false);
        records.setWidth(380);
        stage.show();
        stage.setX(40);
        stage.setY(30);
        stage.setWidth(800);
        stage.setHeight(500);
        stage.hide();

        assertThat(preferences.get("theme", null)).isEqualTo("LIGHT");
        assertThat(preferences.getBoolean("toolWindow.log.visible", true)).isFalse();
        assertThat(preferences.getDouble("toolWindow.records.width", 0)).isEqualTo(380);
        assertThat(preferences.getDouble("window.x", 0)).isEqualTo(40);
        assertThat(preferences.getDouble("window.width", 0)).isEqualTo(800);
        assertThat(preferences.getBoolean("window.maximized", true)).isFalse();
    }

    @Test
    @DisplayName("Leaves as they are the parts it cannot read, and the bounds standing on no screen")
    void leavesUnreadablePartsAlone() {
        preferences.put("theme", "PURPLE");
        preferences.put("toolWindow.records.width", "wide");
        preferences.putDouble("window.x", -100_000);
        preferences.putDouble("window.y", -100_000);
        preferences.putDouble("window.width", 900);
        preferences.putDouble("window.height", 600);
        new WorkbenchState(preferences).manage(stage, workbench, themes);

        assertThat(themes.getMode()).isEqualTo(ThemeMode.SYSTEM);
        assertThat(records.getWidth()).isEqualTo(ToolWindow.DEFAULT_WIDTH);
        assertThat(stage.getX()).isNotEqualTo(-100_000);
    }

    @Test
    @DisplayName("Stops saving once unsubscribed, and logs rather than fails when the store cannot be written")
    void stopsSavingOnceUnsubscribed() {
        Subscription subscription = new WorkbenchState(preferences).manage(stage, workbench, themes);
        subscription.unsubscribe();
        stage.show();
        stage.hide();
        assertThat(preferences.get("theme", null)).isNull();

        WorkbenchState failing = new WorkbenchState(MemoryPreferences.failingToFlush());
        failing.save(themes);
        assertThatNoException().isThrownBy(failing::flush);
    }
}
