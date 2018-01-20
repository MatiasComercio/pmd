/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.nodeevents;

import java.util.Arrays;
import java.util.Objects;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * xnow document
 */
public class NodeEventsRecorderImpl implements NodeEventsRecorder {
    // xnow: document
    // List of node events is ordered based on the child index for the parent node, so, only one node event is recorded for each child of a parent node
    private NodeEvent[] nodeEvents;

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

    @Override
    public boolean hasRewriteEvents() {
        return nodeEvents != null && nodeEvents.length > 0;
    }

    @Override
    public NodeEvent[] getRewriteEvents() {
        // Completely immutable as NodeEvent has all its fields final
        return Arrays.copyOf(nodeEvents, nodeEvents.length);
    }

    private void validateNonNullIndex(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException(String.format("index <%d> is lower than 0", index));
        }
    }

    private void recordNodeEvents(final NodeEvent nodeEvent) {
        final int childIndex = nodeEvent.getChildNodeIndex();
        if (nodeEvents == null) {
            nodeEvents = new NodeEvent[childIndex + 1];
        } else if (childIndex >= nodeEvents.length) {
            final NodeEvent[] newNodeEvents = new NodeEvent[childIndex + 1];
            System.arraycopy(nodeEvents, 0,newNodeEvents, 0, nodeEvents.length);
            nodeEvents = newNodeEvents;
        }

        final NodeEvent oldNodeEvent = nodeEvents[childIndex];
        if (oldNodeEvent == null) { // This is the first event for this child index
            nodeEvents[childIndex] = nodeEvent;
        } else {
            // There is a previous event for the given index => we have to merge the old node event
            //  with the new one before recording the given event
            nodeEvents = recordMergedNodeEvents(nodeEvents, childIndex, oldNodeEvent, nodeEvent);
        }
    }

    private NodeEvent[] recordMergedNodeEvents(final NodeEvent[] nodeEvents, final int childIndex, final NodeEvent oldNodeEvent, final NodeEvent newNodeEvent) {
        final NodeEventType oldNodeEventType = oldNodeEvent.getNodeEventType();
        final NodeEventType newNodeEventType = newNodeEvent.getNodeEventType();
        final NodeEventsMerger nodeEventsMerger = NodeEventsMergers.getNodeEventsMerger(oldNodeEventType, newNodeEventType);
        return nodeEventsMerger.recordMerge(nodeEvents, childIndex, oldNodeEvent, newNodeEvent);
    }
}
