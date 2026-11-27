package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("ContentTabs")
class ContentTabsTest {

    private final Tab first = new Tab("SO-10042", new Label("Ada Lovelace"));
    private final Tab second = new Tab("SO-10043", new Label("Edsger Dijkstra"));
    private final ContentTabs tabs = new ContentTabs(first, second);

    /**
     * Shows the tabs in a scene of a given width, styled and laid out twice, for the nodes skins add while laid out.
     */
    private Scene shown(double width) {
        Scene scene = new Scene(tabs, width, 300);
        for (int pass = 0; pass < 2; pass++) {
            tabs.applyCss();
            tabs.layout();
        }
        return scene;
    }

    private static Action action(String id, String text) {
        return Action.builder(id, text).icon(Icons.EDIT).handler(() -> { }).build();
    }

    private static MouseEvent click(MouseButton button) {
        return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, button, 1, false, false, false, false,
                button == MouseButton.PRIMARY, button == MouseButton.MIDDLE, false, false, false, true, null);
    }

    @Test
    @DisplayName("Closes every tab and reorders them by dragging, unless told otherwise")
    void closesAndReordersTabs() {
        assertThat(tabs.getStyleClass()).contains("tab-pane", "content-tabs");
        assertThat(tabs.getTabClosingPolicy()).isEqualTo(TabPane.TabClosingPolicy.ALL_TABS);
        assertThat(tabs.getTabDragPolicy()).isEqualTo(TabPane.TabDragPolicy.REORDER);
        assertThat(tabs.getTabs()).containsExactly(first, second);
    }

    @Test
    @DisplayName("Shows the buttons of its actions at the end of the row of tabs, which leaves them the room")
    void showsActionsAtEnd() {
        tabs.getActions().setAll(action("test.edit", "Edit"), action("test.copy", "Copy"));
        shown(600);

        Node bar = tabs.lookup(".content-tabs-actions");
        Node header = tabs.lookup(".tab-header-area");
        assertThat(tabs.getSkin()).isInstanceOf(ContentTabsSkin.class);
        assertThat(bar.isVisible()).isTrue();
        assertThat(tabs.lookupAll(".content-tabs-actions > .action-button")).hasSize(2)
                .allMatch(ButtonBase.class::isInstance);
        Bounds barBounds = bar.getBoundsInParent();
        assertThat(barBounds.getMaxX()).isLessThanOrEqualTo(600);
        // The area of the tab headers ends with its padding, empty, its tabs and overflow button standing before it
        assertThat(header.getBoundsInParent().getMaxX() - ((Region) header).snappedRightInset())
                .isLessThanOrEqualTo(barBounds.getMinX());

        tabs.getActions().clear();
        tabs.layout();
        assertThat(bar.isVisible()).isFalse();
    }

    @Test
    @DisplayName("Closes a tab as its close button does: asking it first, then telling it once closed")
    void closesTab() {
        SimpleBooleanProperty closed = new SimpleBooleanProperty();
        first.setOnCloseRequest(event -> event.consume());
        second.setOnClosed(event -> closed.set(true));

        assertThat(tabs.close(first)).isFalse();
        assertThat(tabs.getTabs()).contains(first);
        assertThat(tabs.close(second)).isTrue();
        assertThat(tabs.getTabs()).containsExactly(first);
        assertThat(closed.get()).isTrue();
        assertThat(tabs.close(second)).isFalse();
    }

    @Test
    @DisplayName("Closes the tab a click of the middle button lands on, unless it cannot be closed")
    void closesTabOnMiddleClick() {
        shown(600);
        Node firstHeader = tabs.lookupAll(".tab").stream()
                .filter(node -> node.getProperties().get(Tab.class) == first).findFirst().orElseThrow();
        Node secondHeader = tabs.lookupAll(".tab").stream()
                .filter(node -> node.getProperties().get(Tab.class) == second).findFirst().orElseThrow();

        firstHeader.fireEvent(click(MouseButton.PRIMARY));
        assertThat(tabs.getTabs()).contains(first);
        second.setClosable(false);
        secondHeader.fireEvent(click(MouseButton.MIDDLE));
        assertThat(tabs.getTabs()).contains(second);
        firstHeader.fireEvent(click(MouseButton.MIDDLE));
        assertThat(tabs.getTabs()).containsExactly(second);
    }

    @Test
    @DisplayName("Leaves nothing of its skin behind once given another one")
    void replacesItsSkin() {
        tabs.getActions().add(action("test.edit", "Edit"));
        shown(600);
        tabs.setSkin(new ContentTabsSkin(tabs));

        assertThat(tabs.lookupAll(".content-tabs-actions")).hasSize(1);
    }
}
