/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriteevents;

import net.sourceforge.pmd.lang.ast.Node;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.INSERT;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REMOVE;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REPLACE;

public abstract class RewriteEventFactory {
    public static RewriteEvent newInsertRewriteEvent(final Node parentNode, final Node newChildNode, final int childIndex) {
        return new RewriteEvent(parentNode, null, newChildNode, childIndex);
    }
    public static RewriteEvent newRemoveRewriteEvent(final Node parentNode, final Node oldChildNode, final int childIndex) {
        return new RewriteEvent(parentNode, oldChildNode, null, childIndex);
    }
    public static RewriteEvent newReplaceRewriteEvent(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {
        return new RewriteEvent(parentNode, oldChildNode, newChildNode, childIndex);
    }
}
