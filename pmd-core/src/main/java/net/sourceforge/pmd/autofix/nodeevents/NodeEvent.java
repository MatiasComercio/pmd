/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.nodeevents;

import java.util.Objects;
import net.sourceforge.pmd.lang.ast.Node;

// xnow document
public class NodeEvent {
    private final NodeEventType nodeEventType;
    private final Node parentNode;
    private final Node oldChildNode;
    private final Node newChildNode;
    private final int childNodeIndex;

    public NodeEvent(final NodeEventType nodeEventType,
                     final Node parentNode,
                     final Node oldChildNode,
                     final Node newChildNode,
                     final int childNodeIndex) {
        this.nodeEventType = Objects.requireNonNull(nodeEventType);
        this.parentNode = Objects.requireNonNull(parentNode);
        this.oldChildNode = oldChildNode;
        this.newChildNode = newChildNode;
        this.childNodeIndex = requireNonNegative(childNodeIndex);
    }

    private int requireNonNegative(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException(String.format("n <%d> is lower than 0", n));
        }
        return n;
    }

    public NodeEventType getNodeEventType() {
        return nodeEventType;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public Node getOldChildNode() {
        return oldChildNode;
    }

    public Node getNewChildNode() {
        return newChildNode;
    }

    public int getChildNodeIndex() {
        return childNodeIndex;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final NodeEvent nodeEvent = (NodeEvent) o;
        return childNodeIndex == nodeEvent.childNodeIndex &&
            nodeEventType == nodeEvent.nodeEventType &&
            Objects.equals(parentNode, nodeEvent.parentNode) &&
            Objects.equals(oldChildNode, nodeEvent.oldChildNode) &&
            Objects.equals(newChildNode, nodeEvent.newChildNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeEventType, parentNode, oldChildNode, newChildNode, childNodeIndex);
    }
}
