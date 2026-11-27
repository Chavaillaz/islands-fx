package com.chavaillaz.fx.islands.workbench;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import com.chavaillaz.fx.islands.action.Action;

/**
 * The tabs of the content of the window, as an editor shows the files it opened: a tab for each item open, closed by
 * its button, shown on the tab under the mouse and the one selected, or by a click of the middle button, and moved by
 * dragging it. The buttons of the actions applying to the content stand at the end of the row of tabs, which stands as
 * high as the header of a tool window.
 * <p>
 * It is a {@link TabPane}, closing every tab and reordering them unless told otherwise, whose tabs are those of
 * JavaFX. It carries the style classes {@code tab-pane} and {@code content-tabs}, and the bar of its actions
 * {@code content-tabs-actions}. Lists, tables and trees filling a tab are left without a frame, as in an island.
 */
public final class ContentTabs extends TabPane {

    private final ObservableList<Action> actions = FXCollections.observableArrayList();

    /**
     * Creates the tabs of no item open yet.
     */
    public ContentTabs() {
        getStyleClass().add("content-tabs");
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        setTabDragPolicy(TabDragPolicy.REORDER);
    }

    /**
     * Creates the tabs of the items open.
     *
     * @param tabs the tabs, in their order
     */
    public ContentTabs(Tab... tabs) {
        this();
        getTabs().addAll(tabs);
    }

    /**
     * Gives the actions applying to the content, whose buttons stand at the end of the row of tabs.
     *
     * @return the modifiable list of the actions, in the order of their buttons
     */
    public ObservableList<Action> getActions() {
        return actions;
    }

    /**
     * Closes a tab as its close button does, such as from an action closing the item selected: the tab is asked
     * first, through the handler of its close request, which can keep it open by consuming the event, then told once
     * closed, through its handler of closing.
     *
     * @param tab the tab
     * @return {@code true} when the tab closed, {@code false} when kept open or not one of these tabs
     */
    public boolean close(Tab tab) {
        if (!getTabs().contains(tab)) {
            return false;
        }
        Event request = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, request);
        if (request.isConsumed()) {
            return false;
        }
        getTabs().remove(tab);
        if (tab.getOnClosed() != null) {
            Event.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
        }
        return true;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ContentTabsSkin(this);
    }
}
