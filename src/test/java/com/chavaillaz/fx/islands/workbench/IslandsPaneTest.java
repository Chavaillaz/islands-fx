package com.chavaillaz.fx.islands.workbench;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("IslandsPane")
class IslandsPaneTest {

    private final Region content = new StackPane();
    private final IslandsPane pane = new IslandsPane(content);
    private final Region leftIsland = new StackPane();
    private final Region rightIsland = new StackPane();
    private final ToolWindow left = toolWindow("left", ToolWindowAnchor.LEFT, 300);
    private final ToolWindow right = toolWindow("right", ToolWindowAnchor.RIGHT, 200);

    @Test
    @DisplayName("Gives the tool windows their width, and the content what is left")
    void givesToolWindowsTheirWidth() {
        pane.dock(left, leftIsland);
        pane.dock(right, rightIsland);
        layout(1000);

        assertBounds(leftIsland, 0, 300);
        assertBounds(content, 306, 794);
        assertBounds(rightIsland, 800, 1000);
    }

    @Test
    @DisplayName("Gives the content the room of the tool windows hidden")
    void givesContentRoomOfHiddenToolWindows() {
        pane.dock(left, leftIsland);
        left.setVisible(false);
        layout(1000);

        assertThat(leftIsland.isVisible()).isFalse();
        assertBounds(content, 0, 1000);
    }

    @Test
    @DisplayName("Narrows the tool windows rather than the content when the room lacks")
    void narrowsToolWindowsFirst() {
        left.setWidth(900);
        pane.dock(left, leftIsland);
        layout(1000);

        assertBounds(leftIsland, 0, 1000 - IslandsPane.MIN_CONTENT_WIDTH - IslandsPane.GAP);
        assertBounds(content, 1000 - IslandsPane.MIN_CONTENT_WIDTH, 1000);
    }

    @Test
    @DisplayName("Resizes a tool window by the gap beside it, within the room the content leaves")
    void resizesToolWindowByItsGap() {
        pane.dock(left, leftIsland);
        pane.dock(right, rightIsland);
        layout(1000);

        drag(divider(0), 303, 353);
        assertThat(left.getWidth()).isEqualTo(350);

        drag(divider(1), 797, 747);
        assertThat(right.getWidth()).isEqualTo(250);

        drag(divider(0), 353, 1000);
        assertThat(left.getWidth()).isEqualTo(1000 - IslandsPane.MIN_CONTENT_WIDTH - 2 * IslandsPane.GAP - 250);

        drag(divider(0), 353, 0);
        assertThat(left.getWidth()).isEqualTo(ToolWindow.MIN_WIDTH);
    }

    @Test
    @DisplayName("Asks for the room of the content and of the tool windows showing")
    void asksForTheRoomNeeded() {
        pane.dock(left, leftIsland);
        pane.dock(right, rightIsland);
        right.setVisible(false);

        assertThat(pane.minWidth(-1)).isEqualTo(IslandsPane.MIN_CONTENT_WIDTH);
        assertThat(pane.prefWidth(-1)).isEqualTo(IslandsPane.MIN_CONTENT_WIDTH + 300 + IslandsPane.GAP);
        assertThat(pane.minHeight(-1)).isZero();
        assertThat(pane.prefHeight(-1)).isZero();
    }

    @Test
    @DisplayName("Gives the tool window at the bottom its height, under the content and between those of the sides")
    void givesBottomToolWindowItsHeight() {
        ToolWindow bottom = toolWindow("bottom", ToolWindowAnchor.BOTTOM, 0);
        bottom.setHeight(200);
        Region bottomIsland = new StackPane();
        pane.dock(left, leftIsland);
        pane.dock(bottom, bottomIsland);
        layout(1000);

        assertThat(leftIsland.getBoundsInParent().getHeight()).isEqualTo(500);
        assertThat(content.getBoundsInParent()).satisfies(bounds -> {
            assertThat(bounds.getMinX()).isEqualTo(306);
            assertThat(bounds.getMaxX()).isEqualTo(1000);
            assertThat(bounds.getHeight()).isEqualTo(500 - 200 - IslandsPane.GAP);
        });
        assertThat(bottomIsland.getBoundsInParent()).satisfies(bounds -> {
            assertThat(bounds.getMinX()).isEqualTo(306);
            assertThat(bounds.getMinY()).isEqualTo(300);
            assertThat(bounds.getMaxY()).isEqualTo(500);
        });
        assertThat(pane.prefWidth(-1)).isEqualTo(IslandsPane.MIN_CONTENT_WIDTH + 300 + IslandsPane.GAP);
    }

    @Test
    @DisplayName("Resizes the tool window at the bottom by the gap above it, within the room the content leaves")
    void resizesBottomToolWindowByItsGap() {
        ToolWindow bottom = toolWindow("bottom", ToolWindowAnchor.BOTTOM, 0);
        pane.dock(bottom, new StackPane());
        layout(1000);

        assertThat(divider(0).getCursor()).isEqualTo(Cursor.V_RESIZE);
        dragVertically(divider(0), 257, 207);
        assertThat(bottom.getHeight()).isEqualTo(ToolWindow.DEFAULT_HEIGHT + 50);

        dragVertically(divider(0), 207, 0);
        assertThat(bottom.getHeight()).isEqualTo(500 - IslandsPane.MIN_CONTENT_HEIGHT - IslandsPane.GAP);

        dragVertically(divider(0), 0, 1000);
        assertThat(bottom.getHeight()).isEqualTo(ToolWindow.MIN_HEIGHT);
    }

    private static ToolWindow toolWindow(String id, ToolWindowAnchor anchor, double width) {
        return ToolWindow.builder(id, id, Icons.RECORDS, new StackPane())
                .anchor(anchor)
                .width(width)
                .build();
    }

    private void layout(double width) {
        pane.resize(width, 500);
        pane.layout();
    }

    private Node divider(int index) {
        return pane.getChildrenUnmodifiable().stream()
                .filter(node -> node.getStyleClass().contains("island-divider"))
                .toList()
                .get(index);
    }

    private void drag(Node divider, double fromX, double toX) {
        divider.fireEvent(mouseEvent(MouseEvent.MOUSE_PRESSED, fromX));
        divider.fireEvent(mouseEvent(MouseEvent.MOUSE_DRAGGED, toX));
        pane.layout();
    }

    private void dragVertically(Node divider, double fromY, double toY) {
        divider.fireEvent(new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, fromY, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
        divider.fireEvent(new MouseEvent(MouseEvent.MOUSE_DRAGGED, 0, 0, 0, toY, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
        pane.layout();
    }

    private static MouseEvent mouseEvent(EventType<MouseEvent> type, double screenX) {
        return new MouseEvent(type, 0, 0, screenX, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null);
    }

    private static void assertBounds(Node node, double minX, double maxX) {
        Bounds bounds = node.getBoundsInParent();
        assertThat(bounds.getMinX()).isEqualTo(minX);
        assertThat(bounds.getMaxX()).isEqualTo(maxX);
        assertThat(bounds.getHeight()).isEqualTo(500);
    }
}
