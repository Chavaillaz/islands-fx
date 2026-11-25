package com.chavaillaz.fx.islands.icon;

/**
 * The icons of the library, which its controls show and an application can use as well.
 */
public enum Icons implements Icon {

    /** A plus, adding something. */
    ADD("M8 3.5v9M3.5 8h9"),

    /** An open book, standing for specifications or documentation. */
    BOOK("M8 4.5C6.5 3.5 4.5 3 2.5 3v9c2 0 4 .5 5.5 1.5c1.5-1 3.5-1.5 5.5-1.5V3c-2 0-4 .5-5.5 1.5zM8 4.5v9"),

    /** A line rising over two axes, analysing values in a chart. */
    CHART("M2.5 2.5v11h11M4.5 10.5l3-3 2 2 4-4.5"),

    /** A tick, validating something. */
    CHECK("M3 8.5l3 3 7-7"),

    /** A chevron pointing right, separating the breadcrumbs of a path. */
    CHEVRON_RIGHT("M6.5 4L10.5 8L6.5 12"),

    /** A cross, closing something. */
    CLOSE("M4 4l8 8M12 4l-8 8"),

    /** Two chevrons pointing up, collapsing every node of a tree. */
    COLLAPSE_ALL("M4.5 7.5L8 4l3.5 3.5M4.5 12L8 8.5l3.5 3.5"),

    /** Two arrows pointing at opposite sides, comparing two things. */
    COMPARE("M2.5 5.5h10M10 3l2.5 2.5L10 8M13.5 10.5h-10M6 8l-2.5 2.5L6 13"),

    /** Two pages over each other, copying something. */
    COPY("M5.5 4.5h7a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1h-7a1 1 0 0 1-1-1v-7a1 1 0 0 1 1-1zM2.5 10.5v-7a1 1 0 0 1 1-1h7"),

    /** A bin, deleting something. */
    DELETE("M3 4.5h10M6.5 4.5V3a.5.5 0 0 1 .5-.5h2a.5.5 0 0 1 .5.5v1.5M4.5 4.5l.7 8.6a1 1 0 0 0 1 .9h3.6"
            + "a1 1 0 0 0 1-.9l.7-8.6M6.5 7v4.5M9.5 7v4.5"),

    /** A pencil, editing something. */
    EDIT("M10.5 2.5l3 3-8 8h-3v-3zM9 4l3 3"),

    /** An exclamation mark in a circle, telling an error. */
    ERROR("M14 8a6 6 0 1 1-12 0a6 6 0 1 1 12 0zM8 5v3.5M8 11h.01"),

    /** Two chevrons pointing down, expanding every node of a tree. */
    EXPAND_ALL("M4.5 4L8 7.5L11.5 4M4.5 8.5L8 12l3.5-3.5"),

    /** An arrow rising out of a tray, exporting something. */
    EXPORT("M8 9.5v-7M5 5.5l3-3 3 3M2.5 10v2.5a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1V10"),

    /** A funnel, filtering what is listed. */
    FILTER("M2.5 3.5h11L9 9v4l-2-1.5V9z"),

    /** A folder, standing for a section grouping details. */
    FOLDER("M1.5 4a1.5 1.5 0 0 1 1.5-1.5h3l1.5 1.5h5.5a1.5 1.5 0 0 1 1.5 1.5v6.5a1.5 1.5 0 0 1-1.5 1.5h-10"
            + "a1.5 1.5 0 0 1-1.5-1.5z"),

    /** A question mark in a circle, opening some help. */
    HELP("M14 8a6 6 0 1 1-12 0a6 6 0 1 1 12 0zM6.5 6.5a1.5 1.5 0 1 1 2.1 1.4c-.4.2-.6.5-.6.9v.6M8 11.25h.01"),

    /** A dash, hiding a tool window. */
    HIDE("M3.5 8.5h9"),

    /** An arrow falling into a tray, importing something. */
    IMPORT("M8 2.5v7M5 6.5l3 3 3-3M2.5 10v2.5a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1V10"),

    /** Square brackets around dots, standing for a list of values. */
    LIST("M5.5 2.5h-2v11h2M10.5 2.5h2v11h-2M6 8h.01M8 8h.01M10 8h.01"),

    /** Three lines, opening the main menu where the operating system has no menu bar of its own. */
    MENU("M2.5 4.5h11M2.5 8.5h11M2.5 12.5h11"),

    /** Braces, standing for an object holding values. */
    OBJECT("M6 2.5C4.9 2.5 4.5 3.1 4.5 4.2v1.9c0 1.1-.6 1.9-1.6 1.9c1 0 1.6.8 1.6 1.9v1.9c0 1.1.4 1.7 1.5 1.7"
            + "M10 2.5c1.1 0 1.5.6 1.5 1.7v1.9c0 1.1.6 1.9 1.6 1.9c-1 0-1.6.8-1.6 1.9v1.9c0 1.1-.4 1.7-1.5 1.7"),

    /** An open folder, opening a file. */
    OPEN("M1.5 12.5v-8A1.5 1.5 0 0 1 3 3h3l1.5 1.5h5A1.5 1.5 0 0 1 14 6v1M1.5 12.5l2-5.5h11l-2 5.5z"),

    /** A dot, standing for a single value. */
    PROPERTY("M10.5 8a2.5 2.5 0 1 1-5 0a2.5 2.5 0 1 1 5 0z"),

    /** A page, standing for a record. */
    RECORD("M4.5 1.5h5l3 3v9a1 1 0 0 1-1 1h-7a1 1 0 0 1-1-1v-11a1 1 0 0 1 1-1zM9.5 1.5v3h3M5.5 8.5h5M5.5 11.5h5"),

    /** A table, standing for the records listed. */
    RECORDS("M4 2.5h8A1.5 1.5 0 0 1 13.5 4v8a1.5 1.5 0 0 1-1.5 1.5H4A1.5 1.5 0 0 1 2.5 12V4A1.5 1.5 0 0 1 4 2.5z"
            + "M2.5 6.5h11M2.5 10h11M6.5 6.5v7"),

    /** A circular arrow, loading again. */
    REFRESH("M12.76 10.75A5.5 5.5 0 1 1 11.89 4.11L13.5 5.5M13.5 2.5v3h-3"),

    /** A floppy disk, saving something. */
    SAVE("M3.5 2.5h7l3 3v7a1 1 0 0 1-1 1h-9a1 1 0 0 1-1-1v-9a1 1 0 0 1 1-1zM5.5 2.5v3h4v-3M5 13.5v-4h6v4"),

    /** A magnifier, searching something. */
    SEARCH("M10.5 7a3.5 3.5 0 1 1-7 0a3.5 3.5 0 1 1 7 0zM9.5 9.5l4 4"),

    /** A gear, opening the settings. */
    SETTINGS("M12.76 6.46L14.38 6.76v2.48l-1.62.31A5 5 0 0 1 11.72 11.35l.55 1.56-2.15 1.24-1.08-1.25"
            + "A5 5 0 0 1 6.96 12.89l-1.08 1.25-2.15-1.24.55-1.56A5 5 0 0 1 3.24 9.55L1.62 9.24V6.76l1.62-.31"
            + "A5 5 0 0 1 4.28 4.65l-.55-1.56 2.15-1.24 1.08 1.25A5 5 0 0 1 9.04 3.11l1.08-1.25 2.15 1.24-.55 1.56"
            + "A5 5 0 0 1 12.76 6.46zM10 8a2 2 0 1 1-4 0a2 2 0 1 1 4 0z"),

    /** An exclamation mark in a triangle, warning about something. */
    WARNING("M8 2.5l6 10.5H2zM8 6.5v3M8 11.5h.01");

    private final String path;

    Icons(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }
}
