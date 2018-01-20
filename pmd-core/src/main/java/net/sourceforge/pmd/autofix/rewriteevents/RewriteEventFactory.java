package net.sourceforge.pmd.autofix.rewriteevents;

import net.sourceforge.pmd.lang.ast.Node;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.INSERT;

public abstract class RewriteEventFactory {
    public static RewriteEvent createInsertRewriteEvent(final Node parentNode, final int childIndex, final Node newChildNode) {
        return new RewriteEvent(INSERT, parentNode, null, newChildNode, childIndex);
    }
    public static RewriteEvent createRemoveRewriteEvent(final Node parentNode, final int childIndex, final Node oldChildNode) {
        return new RewriteEvent(RewriteEventType.REMOVE, parentNode, oldChildNode, null, childIndex);
    }
    public static RewriteEvent createReplaceRewriteEvent(final Node parentNode, final int childIndex, final Node oldChildNode, final Node newChildNode) {
        return new RewriteEvent(RewriteEventType.REPLACE, parentNode, oldChildNode, newChildNode, childIndex);
    }
}
