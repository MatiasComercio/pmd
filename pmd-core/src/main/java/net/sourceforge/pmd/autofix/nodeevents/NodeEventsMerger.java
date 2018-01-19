package net.sourceforge.pmd.autofix.nodeevents;

import java.util.List;

public interface NodeEventsMerger {
    void recordMerge(int childIndex, List<NodeEvent> nodeEvents, NodeEvent oldNodeEvent, NodeEvent newNodeEvent);
}
