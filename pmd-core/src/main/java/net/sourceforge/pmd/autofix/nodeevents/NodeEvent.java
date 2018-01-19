/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.nodeevents;

import net.sourceforge.pmd.lang.ast.Node;

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
        this.nodeEventType = nodeEventType;
        this.parentNode = parentNode;
        this.oldChildNode = oldChildNode;
        this.newChildNode = newChildNode;
        this.childNodeIndex = childNodeIndex;
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
}
