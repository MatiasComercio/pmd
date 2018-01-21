/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriteevents;

// xnow document; add clarification saying that index is for
//  adding the possibility of putting these event types in arrays
//  for quicker access (sth like that)
public enum RewriteEventType {
    INSERT(0), REMOVE(1), REPLACE(2);

    private final int index;

    RewriteEventType(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
