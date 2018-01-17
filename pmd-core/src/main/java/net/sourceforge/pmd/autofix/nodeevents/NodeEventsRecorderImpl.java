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

        recordNodeEvent(createRemoveNodeEvent(parentNode, childIndex, oldChildNode));
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

        recordNodeEvent(createInsertNodeEvent(parentNode, childIndex, newChildNode));
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

        recordNodeEvent(createReplaceNodeEvent(parentNode, childIndex, oldChildNode, newChildNode));
    }

    private void recordNodeEvent(final NodeEvent nodeEvent) {
        final Node parentNode = nodeEvent.getParentNode();
        final int childIndex = nodeEvent.getChildNodeIndex();
        List<NodeEvent> nodeEvents = nodeEventsPerNode.get(parentNode);
        if (nodeEvents == null) {
            // Create an array list of at least the child index size
            nodeEvents = new ArrayList<>( 1);
            nodeEventsPerNode.put(parentNode, nodeEvents);
        }
        nodeEvents.add(childIndex, nodeEvent);
    }

    private NodeEvent createInsertNodeEvent(final Node parentNode, final int childIndex, final Node newChildNode) {
        return new NodeEvent(NodeEventType.INSERT, parentNode, null, newChildNode, childIndex);
    }
    private NodeEvent createRemoveNodeEvent(final Node parentNode, final int childIndex, final Node oldChildNode) {
        return new NodeEvent(NodeEventType.REMOVE, parentNode, oldChildNode, null, childIndex);
    }
    private NodeEvent createReplaceNodeEvent(final Node parentNode, final int childIndex, final Node oldChildNode, final Node newChildNode) {
        return new NodeEvent(NodeEventType.REPLACE, parentNode, oldChildNode, newChildNode, childIndex);
    }
}
