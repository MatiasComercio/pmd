/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.nodeevents;

public enum NodeEventType {
    INSERT(0), REMOVE(1), REPLACE(2);

    private final int index;

    NodeEventType(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
