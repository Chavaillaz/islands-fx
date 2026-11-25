package com.chavaillaz.fx.islands.icon;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.geometry.Bounds;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.chavaillaz.fx.islands.JavaFxExtension;

@ExtendWith(JavaFxExtension.class)
@DisplayName("Icon")
class IconTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(Icons.class)
    @DisplayName("Is drawn within its grid")
    void isDrawnWithinGrid(Icon icon) {
        SVGPath shape = new SVGPath();
        shape.setContent(icon.path());
        Bounds bounds = shape.getLayoutBounds();

        assertThat(bounds.getMinX()).isGreaterThanOrEqualTo(0);
        assertThat(bounds.getMinY()).isGreaterThanOrEqualTo(0);
        assertThat(bounds.getMaxX()).isLessThanOrEqualTo(IconView.GRID);
        assertThat(bounds.getMaxY()).isLessThanOrEqualTo(IconView.GRID);
        assertThat(bounds.getWidth()).isPositive();
    }

    @Test
    @DisplayName("Is styled by a class named after it")
    void isStyledByItsClass() {
        assertThat(Icons.CHEVRON_RIGHT.styleClass()).isEqualTo("icon-chevron-right");
    }

    @Test
    @DisplayName("Is shown at the size asked, scaled from its grid")
    void isShownAtSizeAsked() {
        IconView view = new IconView(Icons.SETTINGS, 20);

        assertThat(view.getIcon()).isEqualTo(Icons.SETTINGS);
        assertThat(view.getStyleClass()).contains("icon", "icon-settings");
        assertThat(view.getPrefWidth()).isEqualTo(20);
        assertThat(view.getChildrenUnmodifiable()).singleElement()
                .satisfies(shape -> assertThat(shape.getBoundsInParent().getMaxX()).isLessThanOrEqualTo(20));
        assertThat(view.isMouseTransparent()).isTrue();
        assertThat(new IconView(Icons.HIDE).getPrefWidth()).isEqualTo(IconView.GRID);
    }
}
