package net.sourceforge.pmd.autofix.nodeevents;

import java.util.List;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventFactory.createInsertNodeEvent;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventFactory.createRemoveNodeEvent;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventFactory.createReplaceNodeEvent;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventType.INSERT;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventType.REMOVE;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventType.REPLACE;

public abstract class NodeEventsMergers {
    private static final NodeEventsMerger INSERT_NEW_NODE_EVENT_MERGER = new NodeEventsMerger() {
        @Override
        public void recordMerge(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
            nodeEvents.add(childIndex, newNodeEvent);
        }
    };

    private static final NodeEventsMerger REMOVE_ORIGINAL_NODE_EVENT_MERGER = new NodeEventsMerger() {
        @Override
        public void recordMerge(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
            nodeEvents.remove(childIndex);
        }
    };

    private static final NodeEventsMerger INSERT_NODE_EVENTS_MERGER = new NodeEventsMerger() {
        @Override
        public void recordMerge(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
            final NodeEvent mergedNodeEvent = createInsertNodeEvent(newNodeEvent.getParentNode(), childIndex, newNodeEvent.getNewChildNode());
            nodeEvents.set(childIndex, mergedNodeEvent);
        }
    };

    private static final NodeEventsMerger REPLACE_NODE_EVENTS_MERGER = new NodeEventsMerger() {
        @Override
        public void recordMerge(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
            final NodeEvent mergedNodeEvent = createReplaceNodeEvent(newNodeEvent.getParentNode(), childIndex, oldNodeEvent.getOldChildNode(), newNodeEvent.getNewChildNode());
            nodeEvents.set(childIndex, mergedNodeEvent);
        }
    };

    private static final NodeEventsMerger REMOVE_NODE_EVENTS_MERGER = new NodeEventsMerger() {
        @Override
        public void recordMerge(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
            final NodeEvent mergedNodeEvent = createRemoveNodeEvent(newNodeEvent.getParentNode(), childIndex, oldNodeEvent.getOldChildNode());
            nodeEvents.set(childIndex, mergedNodeEvent);
        }
    };

    private static final NodeEventsMerger INVALID_MERGER = new NodeEventsMerger() {
        @Override
        public void recordMerge(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
            final String msg = String.format("Cannot merge events: <%s> -> <%s>", oldNodeEvent.getNodeEventType(), newNodeEvent.getNodeEventType());
            throw new IllegalStateException(msg);
        }
    };

    private static final NodeEventsMerger[][] NODE_EVENTS_MERGERS;
    static {
        final int size = NodeEventType.values().length;
        NODE_EVENTS_MERGERS = new NodeEventsMerger[size][size];
        final int iInsert = INSERT.getIndex();
        final int iRemove = REMOVE.getIndex();
        final int iReplace = REPLACE.getIndex();

        // Insert -> Insert = both Inserts are kept
        NODE_EVENTS_MERGERS[iInsert][iInsert] = INSERT_NEW_NODE_EVENT_MERGER;

        // Insert -> Replace = Insert, with the newNodeEvent of the Replace event
        NODE_EVENTS_MERGERS[iInsert][iReplace] = INSERT_NODE_EVENTS_MERGER;

        // Insert -> Remove = remove the original Insert event
        NODE_EVENTS_MERGERS[iInsert][iRemove] = REMOVE_ORIGINAL_NODE_EVENT_MERGER;

        // Replace -> Insert = Replace & Insert are kept
        NODE_EVENTS_MERGERS[iReplace][iInsert] = INSERT_NEW_NODE_EVENT_MERGER;

        // Replace -> Replace = Replace, with the oldNodeEvent of the original Replace and the newNodeEvent of the new Replace
        NODE_EVENTS_MERGERS[iReplace][iReplace] = REPLACE_NODE_EVENTS_MERGER;

        // Replace -> Remove = Remove, with the oldNodeEvent of the original Replace
        NODE_EVENTS_MERGERS[iReplace][iRemove] = REMOVE_NODE_EVENTS_MERGER;

        // Remove -> Insert = Replace, with the oldNodeEvent of the Remove and the newNodeEvent of the Insert
        NODE_EVENTS_MERGERS[iRemove][iInsert] = REPLACE_NODE_EVENTS_MERGER;

        // Cannot replace or remove an already removed node
        NODE_EVENTS_MERGERS[iRemove][iReplace] = INVALID_MERGER;
        NODE_EVENTS_MERGERS[iRemove][iRemove] = INVALID_MERGER;
    }

    public static NodeEventsMerger getNodeEventsMerger(final NodeEventType oldEventType, final NodeEventType newEventType) {
        return NODE_EVENTS_MERGERS[oldEventType.getIndex()][newEventType.getIndex()];
    }
}
