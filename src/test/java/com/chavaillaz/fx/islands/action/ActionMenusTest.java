package com.chavaillaz.fx.islands.action;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.chavaillaz.fx.islands.JavaFxExtension;
import com.chavaillaz.fx.islands.icon.IconView;
import com.chavaillaz.fx.islands.icon.Icons;

@ExtendWith(JavaFxExtension.class)
@DisplayName("ActionMenus")
class ActionMenusTest {

    private final SimpleBooleanProperty disabled = new SimpleBooleanProperty();
    private final SimpleBooleanProperty shown = new SimpleBooleanProperty(true);
    private final KeyCombination accelerator = KeyCombination.keyCombination("Shortcut+R");
    private final Action refresh = Action.builder("test.refresh", "Refresh")
            .icon(Icons.REFRESH)
            .accelerator(accelerator)
            .disabled(disabled)
            .handler(() -> { })
            .build();
    private final Action toggle = Action.builder("test.toggle", "Orders")
            .selected(shown)
            .handler(() -> shown.set(!shown.get()))
            .build();

    @Test
    @DisplayName("Shows an action as an item with its text, icon and shortcut, disabled along with it")
    void showsActionAsItem() {
        MenuItem item = ActionMenus.createItem(refresh);

        assertThat(item.getText()).isEqualTo("Refresh");
        assertThat(item.getAccelerator()).isEqualTo(accelerator);
        assertThat(item.getGraphic()).isInstanceOfSatisfying(IconView.class,
                icon -> assertThat(icon.getIcon()).isEqualTo(Icons.REFRESH));
        assertThat(item.isDisable()).isFalse();

        disabled.set(true);
        assertThat(item.isDisable()).isTrue();
    }

    @Test
    @DisplayName("Shows a toggle action as a check item reflecting its state")
    void showsToggleActionAsCheckItem() {
        MenuItem item = ActionMenus.createItem(toggle);

        assertThat(item).isInstanceOfSatisfying(CheckMenuItem.class, checkItem -> {
            assertThat(checkItem.isSelected()).isTrue();
            shown.set(false);
            assertThat(checkItem.isSelected()).isFalse();
            checkItem.fire();
            assertThat(shown.get()).isTrue();
            assertThat(checkItem.isSelected()).isTrue();
        });
    }

    @Test
    @DisplayName("Performs the action of an item when chosen")
    void performsActionWhenChosen() {
        SimpleBooleanProperty performed = new SimpleBooleanProperty();
        MenuItem item = ActionMenus.createItem(Action.builder("test.run", "Run")
                .handler(() -> performed.set(true))
                .build());
        item.fire();

        assertThat(performed.get()).isTrue();
        assertThat(item.getGraphic()).isNull();
    }

    @Test
    @DisplayName("Shows a group as a menu, holding its separators and submenus")
    void showsGroupAsMenu() {
        ActionGroup group = ActionGroup.of("View",
                ActionGroup.of("Tool Windows", toggle),
                ActionSeparator.INSTANCE,
                refresh);
        List<Menu> menus = ActionMenus.createMenus(List.of(group));

        assertThat(menus).singleElement().satisfies(menu -> {
            assertThat(menu.getText()).isEqualTo("View");
            assertThat(menu.getItems()).hasSize(3);
            assertThat(menu.getItems().get(0)).isInstanceOfSatisfying(Menu.class,
                    submenu -> assertThat(submenu.getItems()).singleElement()
                            .extracting(MenuItem::getText).isEqualTo("Orders"));
            assertThat(menu.getItems().get(1)).isInstanceOf(SeparatorMenuItem.class);
            assertThat(menu.getItems().get(2).getText()).isEqualTo("Refresh");
        });
    }
}
