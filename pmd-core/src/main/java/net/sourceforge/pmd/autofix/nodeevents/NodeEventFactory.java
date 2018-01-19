package net.sourceforge.pmd.autofix.nodeevents;

import net.sourceforge.pmd.lang.ast.Node;
import static net.sourceforge.pmd.autofix.nodeevents.NodeEventType.INSERT;

public abstract class NodeEventFactory {
    public static NodeEvent createInsertNodeEvent(final Node parentNode, final int childIndex, final Node newChildNode) {
        return new NodeEvent(INSERT, parentNode, null, newChildNode, childIndex);
    }
    public static NodeEvent createRemoveNodeEvent(final Node parentNode, final int childIndex, final Node oldChildNode) {
        return new NodeEvent(NodeEventType.REMOVE, parentNode, oldChildNode, null, childIndex);
    }
    public static NodeEvent createReplaceNodeEvent(final Node parentNode, final int childIndex, final Node oldChildNode, final Node newChildNode) {
        return new NodeEvent(NodeEventType.REPLACE, parentNode, oldChildNode, newChildNode, childIndex);
    }
}
