/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.nodeevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * xnow document
 */
public class NodeEventsRecorderImpl implements NodeEventsRecorder {
    // xnow: document
    // List of node events is ordered based on the child index for the parent node, so, only one node event is recorded for each child of a parent node
    private final Map<Node, List<NodeEvent>> nodeEventsPerNode;

    public NodeEventsRecorderImpl() {
        this.nodeEventsPerNode = new HashMap<>();
    }

    @Override
    public void recordRemove(final Node parentNode, final Node oldChildNode, final int childIndex) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(oldChildNode);
        validateNonNullIndex(childIndex);

        recordNodeEvents(NodeEventFactory.createRemoveNodeEvent(parentNode, childIndex, oldChildNode));
    }

    @Override
    public void recordInsert(final Node parentNode, final Node newChildNode, final int childIndex) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(newChildNode);
        validateNonNullIndex(childIndex);

        recordNodeEvents(NodeEventFactory.createInsertNodeEvent(parentNode, childIndex, newChildNode));
    }

    @Override
    public void recordReplace(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(oldChildNode);
        Objects.requireNonNull(newChildNode);
        validateNonNullIndex(childIndex);

        recordNodeEvents(NodeEventFactory.createReplaceNodeEvent(parentNode, childIndex, oldChildNode, newChildNode));
    }

    private void validateNonNullIndex(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException(String.format("index <%d> is lower than 0", index));
        }
    }

    private void recordNodeEvents(final NodeEvent nodeEvent) {
        final Node parentNode = nodeEvent.getParentNode();
        final int childIndex = nodeEvent.getChildNodeIndex();
        List<NodeEvent> nodeEvents = nodeEventsPerNode.get(parentNode);
        if (nodeEvents == null) {
            // Create an array list of at least the child index size
            nodeEvents = new ArrayList<>( childIndex + 1);
            nodeEventsPerNode.put(parentNode, nodeEvents);
        }
        final NodeEvent oldNodeEvent = childIndex >= nodeEvents.size() ? null : nodeEvents.get(childIndex);
        if (oldNodeEvent == null) { // This is the first event for this child index
            nodeEvents.set(childIndex, nodeEvent); // set == add in this case, as there is nothing at this index
        } else {
            // There is a previous event for the given index => we have to merge the old node event
            //  with the new one before recording the given event
            recordMergedNodeEvents(childIndex, nodeEvents, oldNodeEvent, nodeEvent);
        }
    }

    private void recordMergedNodeEvents(final int childIndex, final List<NodeEvent> nodeEvents, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
        final NodeEventType oldNodeEventType = oldNodeEvent.getNodeEventType();
        final NodeEventType newNodeEventType = newNodeEvent.getNodeEventType();
        final NodeEventsMerger nodeEventsMerger = NodeEventsMergers.getNodeEventsMerger(oldNodeEventType, newNodeEventType);
        nodeEventsMerger.recordMerge(childIndex, nodeEvents, oldNodeEvent, newNodeEvent);
    }
}
