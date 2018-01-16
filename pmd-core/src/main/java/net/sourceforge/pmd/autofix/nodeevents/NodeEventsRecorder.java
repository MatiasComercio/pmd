package net.sourceforge.pmd.autofix.nodeevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * xnow document
 */
public class NodeEventsRecorder {
    private static final Logger LOGGER = Logger.getLogger(NodeEventsRecorder.class.getName());

    // xnow: document
    // List of node events is ordered based on the child index for the parent node, so, only one node event is recorded for each child of a parent node
    private final Map<Node, List<NodeEvent>> nodeEventsPerNode;

    public NodeEventsRecorder() {
        this.nodeEventsPerNode = new HashMap<>(); // xnow: perhaps making this class a singleton and calling startRecording/endRecording, which will set/clear this variable
    }

    // xnow document
    // assumes that these children have its context (parent node, index, etc.) correctly set, or that they will be correct in the moment of usage
    public void record(final Node parentNode, final int childIndex, final Node oldChildNode, final Node newChildNode) {
        LOGGER.finest(String.format("record - parentNode: <%s>, oldChildNode: <%s>, newChildNode: <%s>",
            parentNode, oldChildNode, newChildNode));

        if (parentNode == null || childIndex < 0) {
            final String msg = String.format("parentNode <%s> cannot be null and childIndex <%d> should be greater than 0", parentNode, childIndex);
            throw new IllegalArgumentException(msg);
        }

        final NodeEvent nodeEvent;
        if (oldChildNode == newChildNode // object equals in case any of them is null
            || (nodeEvent = createNodeEvent(parentNode, childIndex, oldChildNode, newChildNode)) == null) {
            LOGGER.warning("record - oldChildNode and newChildNode are the same object. Skipping...");
            return;
        }

        recordNodeEvent(nodeEvent);
    }

    public void recordInsert(final Node parentNode, final int childIndex, final Node newChildNode) {
        if (parentNode == null || childIndex < 0 || newChildNode == null) {
            final String msg = String.format("parentNode <%s> is null " +
                "or childIndex <%d> is lower than 0" +
                "or newChildNode <%s> is null", parentNode, childIndex, newChildNode);
            throw new IllegalArgumentException(msg);
        }

        recordNodeEvent(createInsertNodeEvent(parentNode, childIndex, newChildNode));
    }

    public void recordRemove(final Node parentNode, final int childIndex, final Node oldChildNode) {
        if (parentNode == null || childIndex < 0 || oldChildNode == null) {
            final String msg = String.format("parentNode <%s> is null " +
                "or childIndex <%d> is lower than 0" +
                "or oldChildNode <%s> is null", parentNode, childIndex, oldChildNode);
            throw new IllegalArgumentException(msg);
        }

        recordNodeEvent(createInsertNodeEvent(parentNode, childIndex, oldChildNode));
    }

    public void recordReplace(final Node parentNode, final int childIndex, final Node oldChildNode, final Node newChildNode) {
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
            nodeEvents = new ArrayList<>( + 1);
            nodeEventsPerNode.put(parentNode, nodeEvents);
        }
        nodeEvents.add(childIndex, nodeEvent);
    }

    private NodeEvent createNodeEvent(final Node parentNode, final int childIndex, final Node oldChildNode, final Node newChildNode) {
        if (oldChildNode == null) {
            LOGGER.finest("record - creating INSERT node event");
            return createInsertNodeEvent(parentNode, childIndex, newChildNode);
        }

        if (newChildNode == null) {
            LOGGER.finest("record - creating REMOVE node event");
            return createRemoveNodeEvent(parentNode, childIndex, oldChildNode);
        }

        return newChildNode.equals(oldChildNode) ? null
            : createReplaceNodeEvent(parentNode, childIndex, oldChildNode, newChildNode);
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
