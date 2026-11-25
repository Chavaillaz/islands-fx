package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("ToolWindow")
class ToolWindowTest {

    @Test
    @DisplayName("Docks showing on the left at the default width unless told otherwise")
    void docksOnLeftByDefault() {
        StackPane content = new StackPane();
        ToolWindow toolWindow = ToolWindow.builder("records", "Orders", Icons.RECORDS, content).build();

        assertThat(toolWindow.getId()).isEqualTo("records");
        assertThat(toolWindow.getTitle()).isEqualTo("Orders");
        assertThat(toolWindow.getIcon()).isEqualTo(Icons.RECORDS);
        assertThat(toolWindow.getContent()).isSameAs(content);
        assertThat(toolWindow.getAnchor()).isEqualTo(ToolWindowAnchor.LEFT);
        assertThat(toolWindow.getWidth()).isEqualTo(ToolWindow.DEFAULT_WIDTH);
        assertThat(toolWindow.isVisible()).isTrue();
        assertThat(toolWindow.getActions()).isEmpty();
        assertThat(toolWindow.getToggleAction().getAccelerator()).isNull();
    }

    @Test
    @DisplayName("Shows and hides through its toggle action, which reflects whether it shows")
    void togglesThroughItsAction() {
        KeyCombination accelerator = KeyCombination.keyCombination("Shortcut+1");
        Action refresh = Action.builder("test.refresh", "Refresh").handler(() -> { }).build();
        ToolWindow toolWindow = ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane())
                .anchor(ToolWindowAnchor.RIGHT)
                .width(300)
                .accelerator(accelerator)
                .actions(refresh)
                .build();
        Action toggle = toolWindow.getToggleAction();

        assertThat(toolWindow.getAnchor()).isEqualTo(ToolWindowAnchor.RIGHT);
        assertThat(toolWindow.getWidth()).isEqualTo(300);
        assertThat(toolWindow.getActions()).containsExactly(refresh);
        assertThat(toggle.getText()).isEqualTo("Orders");
        assertThat(toggle.getAccelerator()).isEqualTo(accelerator);
        assertThat(toggle.isSelected()).isTrue();

        toggle.perform();
        assertThat(toolWindow.isVisible()).isFalse();
        assertThat(toggle.isSelected()).isFalse();

        toggle.perform();
        assertThat(toolWindow.visibleProperty().get()).isTrue();
    }

    @Test
    @DisplayName("Hides through its hide action")
    void hidesThroughItsAction() {
        ToolWindow toolWindow = ToolWindow.builder("records", "Orders", Icons.RECORDS, new StackPane()).build();
        toolWindow.getHideAction().perform();
        toolWindow.getHideAction().perform();

        assertThat(toolWindow.isVisible()).isFalse();
        assertThat(toolWindow.getHideAction().getIcon()).isEqualTo(Icons.HIDE);
    }
}
