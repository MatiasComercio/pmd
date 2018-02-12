/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import net.sourceforge.pmd.lang.ast.Node;

// xaf: update documentation
public class RewriteEvent {
    private final Node oldChild;
    private final Node newChild;

    public RewriteEvent(final Node theOldChild,
                        final Node theNewChild) {
        this.oldChild = theOldChild;
        this.newChild = theNewChild;
    }
}
