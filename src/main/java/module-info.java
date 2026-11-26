/**
 * Islands FX: light and dark themes for JavaFX applications, and the workbench of their main window, its main toolbar
 * standing in the title bar, beside the tool windows and status bar.
 * <ul>
 *     <li>{@link com.chavaillaz.fx.islands.workbench}: the layout of the main window, its toolbar, tool windows and
 *     status bar</li>
 *     <li>{@link com.chavaillaz.fx.islands.action}: the actions of the user, shown as menu items and toolbar
 *     buttons</li>
 *     <li>{@link com.chavaillaz.fx.islands.theme}: the themes, and the manager applying the one chosen to every
 *     window</li>
 *     <li>{@link com.chavaillaz.fx.islands.icon}: the icons, outlines the theme colors</li>
 *     <li>{@link com.chavaillaz.fx.islands.control}: small controls, such as badges and empty states</li>
 * </ul>
 * JavaFX, which the application provides, JSpecify, and the preferences of Java, where the state of the main window is
 * kept, are required transitively, the API of this module exposing their types.
 */
module com.chavaillaz.fx.islands {

    requires transitive java.prefs;
    requires transitive javafx.controls;
    requires transitive org.jspecify;

    exports com.chavaillaz.fx.islands.action;
    exports com.chavaillaz.fx.islands.control;
    exports com.chavaillaz.fx.islands.icon;
    exports com.chavaillaz.fx.islands.theme;
    exports com.chavaillaz.fx.islands.workbench;

}
