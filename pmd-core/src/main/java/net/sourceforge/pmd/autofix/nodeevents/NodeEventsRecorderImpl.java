/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.nodeevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import net.sourceforge.pmd.lang.ast.Node;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventType.INSERT;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventType.REMOVE;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventType.REPLACE;

/**
 * xnow document
 */
public class NodeEventsRecorderImpl implements NodeEventsRecorder {
    private static final Logger LOGGER = Logger.getLogger(NodeEventsRecorderImpl.class.getName());

    // xnow: document
    // List of node events is ordered based on the child index for the parent node, so, only one node event is recorded for each child of a parent node
    private final Map<Node, List<NodeEvent>> nodeEventsPerNode;

    public NodeEventsRecorderImpl() {
        this.nodeEventsPerNode = new HashMap<>(); // xnow: perhaps making this class a singleton and calling startRecording/endRecording, which will set/clear this variable
    }

    @Override
    public void recordRemove(final Node parentNode, final Node oldChildNode, final int childIndex) {
        if (parentNode == null || childIndex < 0 || oldChildNode == null) {
            final String msg = String.format("parentNode <%s> is null " +
                "or childIndex <%d> is lower than 0" +
                "or oldChildNode <%s> is null", parentNode, childIndex, oldChildNode);
            throw new IllegalArgumentException(msg);
        }

        recordMergedNodeEvents(createRemoveNodeEvent(parentNode, childIndex, oldChildNode));
    }

    @Override
    public void recordInsert(final Node parentNode, final Node newChildNode, final int childIndex) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(newChildNode);

        if (childIndex < 0) { // TODO: doing
            final String msg = String.format("parentNode <%s> is null " +
                "or childIndex <%d> is lower than 0" +
                "or newChildNode <%s> is null", parentNode, childIndex, newChildNode);
            throw new IllegalArgumentException(msg);
        }

        recordMergedNodeEvents(createInsertNodeEvent(parentNode, childIndex, newChildNode));
    }

    @Override
    public void recordReplace(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {
        if (parentNode == null || childIndex < 0 || oldChildNode == null || newChildNode == null) {
            final String msg = String.format("parentNode <%s> is null " +
                "or childIndex <%d> is lower than 0 " +
                "or oldChildNode <%s> is null " +
                "or newChildNode <%s> is null", parentNode, childIndex, oldChildNode, newChildNode);
            throw new IllegalArgumentException(msg);
        }

        recordMergedNodeEvents(createReplaceNodeEvent(parentNode, childIndex, oldChildNode, newChildNode));
    }

    private void recordMergedNodeEvents(final NodeEvent nodeEvent) {
        final Node parentNode = nodeEvent.getParentNode();
        final int childIndex = nodeEvent.getChildNodeIndex();
        List<NodeEvent> nodeEvents = nodeEventsPerNode.get(parentNode);
        if (nodeEvents == null) {
            // Create an array list of at least the child index size
            nodeEvents = new ArrayList<>( childIndex + 1);
            nodeEventsPerNode.put(parentNode, nodeEvents);
        }
        final NodeEvent oldNodeEvent = childIndex >= nodeEvents.size() ? null : nodeEvents.get(childIndex);
        if (oldNodeEvent == null) {
            nodeEvents.set(childIndex, nodeEvent); // set == add in this case
        } else {
            recordMergedNodeEvents(childIndex, nodeEvents, oldNodeEvent, nodeEvent);
        }
    }

    private void recordMergedNodeEvents(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
        final NodeEventType oldNodeEventType = oldNodeEvent.getNodeEventType();
        final NodeEventType newNodeEventType = newNodeEvent.getNodeEventType();
        final NodeEventsMerger nodeEventsMerger = NodeEventsMergers.getNodeEventsMerger(oldNodeEventType, newNodeEventType);
        nodeEventsMerger.recordMerge(childIndex, nodeEvents, oldNodeEvent, newNodeEvent);
    }

    private static NodeEvent createInsertNodeEvent(final Node parentNode, final int childIndex, final Node newChildNode) {
        return new NodeEvent(INSERT, parentNode, null, newChildNode, childIndex);
    }
    private static NodeEvent createRemoveNodeEvent(final Node parentNode, final int childIndex, final Node oldChildNode) {
        return new NodeEvent(NodeEventType.REMOVE, parentNode, oldChildNode, null, childIndex);
    }
    private static NodeEvent createReplaceNodeEvent(final Node parentNode, final int childIndex, final Node oldChildNode, final Node newChildNode) {
        return new NodeEvent(NodeEventType.REPLACE, parentNode, oldChildNode, newChildNode, childIndex);
    }

    private static abstract class NodeEventsMergers {
        private static final NodeEventsMerger INSERT_NEW_NODE_EVENTS_MERGER = new NodeEventsMerger() {
            @Override
            public void recordMerge(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
                nodeEvents.add(childIndex, newNodeEvent);
            }
        };

        private static final NodeEventsMerger REMOVE_ORIGINAL_NODE_EVENTS_MERGER = new NodeEventsMerger() {
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

            // Insert -> Insert = 2 Inserts
            NODE_EVENTS_MERGERS[iInsert][iInsert] = INSERT_NEW_NODE_EVENTS_MERGER;

            // Insert -> Replace = Insert, with the newNodeEvent of the replace event
            NODE_EVENTS_MERGERS[iInsert][iReplace] = INSERT_NODE_EVENTS_MERGER;

            // Insert -> Remove = delete the original insert event
            NODE_EVENTS_MERGERS[iInsert][iRemove] = REMOVE_ORIGINAL_NODE_EVENTS_MERGER;

            // Replace -> Insert = Replace & Insert are kept
            NODE_EVENTS_MERGERS[iReplace][iInsert] = INSERT_NEW_NODE_EVENTS_MERGER;

            // Replace -> Replace = Replace, with the oldNodeEvent of the original replace and the newNodeEvent of the new replace
            NODE_EVENTS_MERGERS[iReplace][iReplace] = REPLACE_NODE_EVENTS_MERGER;

            // Replace -> Remove = Remove, with the oldNodeEvent of the original replace
            NODE_EVENTS_MERGERS[iReplace][iRemove] = REMOVE_NODE_EVENTS_MERGER;

            // Remove -> Insert = Replace, with the oldNodeEvent of the remove and the newNodeEvent of the insert
            NODE_EVENTS_MERGERS[iRemove][iInsert] = REPLACE_NODE_EVENTS_MERGER;

            // Cannot replace or remove an already removed node
            NODE_EVENTS_MERGERS[iRemove][iReplace] = INVALID_MERGER;
            NODE_EVENTS_MERGERS[iRemove][iRemove] = INVALID_MERGER;
        }

        public static NodeEventsMerger getNodeEventsMerger(final NodeEventType oldEventType, final NodeEventType newEventType) {
            return NODE_EVENTS_MERGERS[oldEventType.getIndex()][newEventType.getIndex()];
        }
    }
}
