package net.sourceforge.pmd.autofix.nodeevents;

public interface NodeEventsMerger {
    // xnow document
    NodeEvent[] recordMerge(NodeEvent[] nodeEvents, int childIndex, NodeEvent oldNodeEvent, NodeEvent newNodeEvent);
}
