package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("ContentHeader")
class ContentHeaderTest {

    @Test
    @DisplayName("Names what the content shows in a tab after its icon, the buttons of its actions at its end")
    void namesContentInTab() {
        ContentHeader header = new ContentHeader("SO-10042", Icons.RECORD);
        header.getActions().setAll(
                Action.builder("test.expand", "Expand All").icon(Icons.EXPAND_ALL).handler(() -> { }).build(),
                Action.builder("test.collapse", "Collapse All").icon(Icons.COLLAPSE_ALL).handler(() -> { }).build());

        assertThat(header.getStyleClass()).contains("content-header");
        assertThat(header.getChildren().getFirst()).isInstanceOfSatisfying(Label.class, tab -> {
            assertThat(tab.getStyleClass()).contains("content-tab");
            assertThat(tab.getText()).isEqualTo("SO-10042");
            assertThat(((IconView) tab.getGraphic()).getIcon()).isEqualTo(Icons.RECORD);
        });
        assertThat(header.getChildren()).filteredOn(ButtonBase.class::isInstance).hasSize(2);
        assertThat(header.getChildren().getLast().getStyleClass()).contains("action-button");
    }

    @Test
    @DisplayName("Hides its tab while it has no title")
    void hidesTabWithoutTitle() {
        ContentHeader header = new ContentHeader();
        Label tab = (Label) header.getChildren().getFirst();
        assertThat(tab.isManaged()).isFalse();
        assertThat(header.getIcon()).isNull();

        header.setTitle("SO-10042");
        header.setIcon(Icons.RECORD);
        assertThat(tab.isManaged()).isTrue();
        assertThat(header.getTitle()).isEqualTo("SO-10042");
        assertThat(header.getIcon()).isEqualTo(Icons.RECORD);

        header.setIcon(null);
        assertThat(tab.getGraphic()).isNull();
    }
}
