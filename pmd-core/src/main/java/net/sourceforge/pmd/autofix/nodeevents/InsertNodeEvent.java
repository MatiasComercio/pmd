package net.sourceforge.pmd.autofix.nodeevents;

import net.sourceforge.pmd.lang.ast.Node;

public class InsertNodeEvent extends NodeEvent {
    public InsertNodeEvent(final Node parentNode, final Node newChildNode, final int childNodeIndex) {
        super(NodeEventType.INSERT, parentNode, null, newChildNode, childNodeIndex);
    }
}
