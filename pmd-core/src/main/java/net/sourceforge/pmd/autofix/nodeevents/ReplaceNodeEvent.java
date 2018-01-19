package net.sourceforge.pmd.autofix.nodeevents;

import net.sourceforge.pmd.lang.ast.Node;

public class ReplaceNodeEvent extends NodeEvent {
    public ReplaceNodeEvent(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childNodeIndex) {
        super(NodeEventType.REPLACE, parentNode, oldChildNode, newChildNode, childNodeIndex);
    }
}
