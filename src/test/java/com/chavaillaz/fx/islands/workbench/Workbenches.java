package com.chavaillaz.fx.islands.workbench;

/**
 * Creates the workbenches that the tests of other packages render, such as the one of the showcase.
 */
public final class Workbenches {

    private Workbenches() {
    }

    /**
     * Creates a workbench showing its main menu in its main toolbar, as on Windows and Linux, whatever the operating
     * system running the tests: the headless platform of JavaFX has no menu bar of the system to show it in.
     *
     * @return the workbench
     */
    public static Workbench withMenuInToolbar() {
        return new Workbench(false);
    }
}
