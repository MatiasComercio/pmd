package net.sourceforge.pmd.autofix.rewriteevents;

public interface RewriteEventsMerger {
    // xnow document
    RewriteEvent[] recordMerge(RewriteEvent[] rewriteEvents, int childIndex, RewriteEvent oldRewriteEvent, RewriteEvent newRewriteEvent);
}
