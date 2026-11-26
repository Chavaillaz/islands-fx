package com.chavaillaz.fx.islands.workbench;

import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.Nullable;

import com.chavaillaz.fx.islands.action.Action;
import com.chavaillaz.fx.islands.action.ActionButtons;
import com.chavaillaz.fx.islands.icon.Icon;
import com.chavaillaz.fx.islands.icon.IconView;

/**
 * The header of the content of the window: a tab naming what the content shows, after its icon, as an editor shows a
 * file under its tab, then the buttons of the actions applying to it. It stands as high as the header of a tool
 * window.
 * <p>
 * It carries the style class {@code content-header}, and its tab {@code content-tab}. An FXML file declares it as
 * {@code <ContentHeader title="SO-10042" icon="RECORD"/>}.
 */
public final class ContentHeader extends HBox {

    private final Label tab = new Label();
    private final ObjectProperty<@Nullable Icon> icon = new SimpleObjectProperty<>(this, "icon");
    private final ObservableList<Action> actions = FXCollections.observableArrayList();

    /**
     * Creates a header of no title, icon nor action yet.
     */
    public ContentHeader() {
        getStyleClass().add("content-header");
        tab.getStyleClass().add("content-tab");
        tab.setMnemonicParsing(false);
        tab.visibleProperty().bind(tab.textProperty().isNotEmpty());
        tab.managedProperty().bind(tab.visibleProperty());
        icon.subscribe(shown -> tab.setGraphic(shown == null ? null : new IconView(shown)));
        actions.addListener((InvalidationListener) observable -> showActions());
        showActions();
    }

    /**
     * Creates a header of a title and an icon.
     *
     * @param title what the content shows, such as the name of a record
     * @param icon  the icon before the title, or {@code null} for none
     */
    public ContentHeader(@NamedArg("title") String title, @NamedArg("icon") @Nullable Icon icon) {
        this();
        setTitle(title);
        setIcon(icon);
    }

    /**
     * Gives the title of the tab, naming what the content shows.
     *
     * @return the property of the title, empty while the tab is hidden
     */
    public StringProperty titleProperty() {
        return tab.textProperty();
    }

    /**
     * Gives the title of the tab.
     *
     * @return the title, empty for none
     */
    public String getTitle() {
        return tab.getText();
    }

    /**
     * Changes the title of the tab.
     *
     * @param title the title, empty to hide the tab
     */
    public void setTitle(String title) {
        tab.setText(title);
    }

    /**
     * Gives the icon before the title.
     *
     * @return the property of the icon, holding {@code null} for none
     */
    public ObjectProperty<@Nullable Icon> iconProperty() {
        return icon;
    }

    /**
     * Gives the icon before the title.
     *
     * @return the icon, or {@code null} for none
     */
    public @Nullable Icon getIcon() {
        return icon.get();
    }

    /**
     * Changes the icon before the title.
     *
     * @param icon the icon, or {@code null} for none
     */
    public void setIcon(@Nullable Icon icon) {
        this.icon.set(icon);
    }

    /**
     * Gives the actions applying to the content, whose buttons stand at the end of the header.
     *
     * @return the modifiable list of the actions, in the order of their buttons
     */
    public ObservableList<Action> getActions() {
        return actions;
    }

    private void showActions() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().setAll(tab, spacer);
        actions.forEach(action -> getChildren().add(ActionButtons.create(action)));
    }
}
