package com.chavaillaz.fx.islands.workbench;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Preferences kept in memory, so that the tests never read nor write the preferences of the user running them.
 */
final class MemoryPreferences extends AbstractPreferences {

    private final Map<String, String> values = new HashMap<>();
    private final Map<String, MemoryPreferences> children = new HashMap<>();
    private final boolean failingToFlush;

    /**
     * Creates the root of a tree of preferences kept in memory.
     */
    MemoryPreferences() {
        this(null, "", false);
    }

    private MemoryPreferences(MemoryPreferences parent, String name, boolean failingToFlush) {
        super(parent, name);
        this.failingToFlush = failingToFlush;
    }

    /**
     * Creates the root of a tree of preferences whose store cannot be written.
     *
     * @return the preferences, failing when flushed
     */
    static MemoryPreferences failingToFlush() {
        return new MemoryPreferences(null, "", true);
    }

    @Override
    protected void putSpi(String key, String value) {
        values.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
        return values.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        values.remove(key);
    }

    @Override
    protected void removeNodeSpi() {
        values.clear();
    }

    @Override
    protected String[] keysSpi() {
        return values.keySet().toArray(String[]::new);
    }

    @Override
    protected String[] childrenNamesSpi() {
        return children.keySet().toArray(String[]::new);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        return children.computeIfAbsent(name, child -> new MemoryPreferences(this, child, failingToFlush));
    }

    @Override
    protected void syncSpi() {
        // Nothing is stored anywhere else
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        if (failingToFlush) {
            throw new BackingStoreException("The store is read-only");
        }
    }
}
