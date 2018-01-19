package net.sourceforge.pmd.autofix.nodeevents;

import net.sourceforge.pmd.lang.ast.Node;

public class RemoveNodeEvent extends NodeEvent {
    public RemoveNodeEvent(final Node parentNode, final Node oldChildNode, final int childNodeIndex) {
        super(NodeEventType.REMOVE, parentNode, oldChildNode, null, childNodeIndex);
    }
}
