package net.sourceforge.pmd.autofix.rewriteevents;

import net.sourceforge.pmd.lang.ast.Node;

public interface RewriteEventsRecorder {
    // xnow document
    void recordRemove(Node parentNode, Node oldChildNode, int childIndex);

    // xnow document
    void recordInsert(Node parentNode, Node newChildNode, int childIndex);

    // xnow document
    void recordReplace(Node parentNode, Node oldChildNode, Node newChildNode, int childIndex);

    // xnow document
    boolean hasRewriteEvents();

    // xnow document
    RewriteEvent[] getRewriteEvents();
}
