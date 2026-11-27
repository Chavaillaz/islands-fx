package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
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

    /**
     * Gives a header its skin, which a scene creates once styling it.
     */
    private static ContentHeader skinned(ContentHeader header) {
        new Scene(new StackPane(header));
        header.applyCss();
        return header;
    }

    @Test
    @DisplayName("Names what the content shows in a tab after its icon, the buttons of its actions at its end")
    void namesContentInTab() {
        ContentHeader header = new ContentHeader("SO-10042", Icons.RECORD);
        header.getActions().setAll(
                Action.builder("test.expand", "Expand All").icon(Icons.EXPAND_ALL).handler(() -> { }).build(),
                Action.builder("test.collapse", "Collapse All").icon(Icons.COLLAPSE_ALL).handler(() -> { }).build());
        skinned(header);

        assertThat(header.getStyleClass()).contains("content-header");
        assertThat(header.getSkin()).isInstanceOf(ContentHeaderSkin.class);
        assertThat(header.isFocusTraversable()).isFalse();
        assertThat(header.lookup(".content-tab")).isInstanceOfSatisfying(Label.class, tab -> {
            assertThat(tab.getText()).isEqualTo("SO-10042");
            assertThat(((IconView) tab.getGraphic()).getIcon()).isEqualTo(Icons.RECORD);
        });
        assertThat(header.lookupAll(".action-button")).hasSize(2).allMatch(ButtonBase.class::isInstance);

        header.getActions().removeFirst();
        assertThat(header.lookupAll(".action-button")).hasSize(1);
    }

    @Test
    @DisplayName("Hides its tab while it has no title")
    void hidesTabWithoutTitle() {
        ContentHeader header = skinned(new ContentHeader());
        Label tab = (Label) header.lookup(".content-tab");
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

    @Test
    @DisplayName("Leaves nothing of its skin behind once given another one")
    void replacesItsSkin() {
        ContentHeader header = skinned(new ContentHeader("SO-10042", Icons.RECORD));
        header.setSkin(new ContentHeaderSkin(header));

        assertThat(header.lookupAll(".content-tab")).hasSize(1);
    }
}
