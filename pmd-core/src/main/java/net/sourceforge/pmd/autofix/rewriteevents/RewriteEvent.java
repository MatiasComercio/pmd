/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriteevents;

import java.util.Objects;
import net.sourceforge.pmd.lang.ast.Node;

// xnow document
public class RewriteEvent {
    private final RewriteEventType rewriteEventType;
    private final Node parentNode;
    private final Node oldChildNode;
    private final Node newChildNode;
    private final int childNodeIndex;

    public RewriteEvent(final RewriteEventType rewriteEventType,
                        final Node parentNode,
                        final Node oldChildNode,
                        final Node newChildNode,
                        final int childNodeIndex) {
        this.rewriteEventType = Objects.requireNonNull(rewriteEventType);
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

    public RewriteEventType getRewriteEventType() {
        return rewriteEventType;
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
        final RewriteEvent rewriteEvent = (RewriteEvent) o;
        return childNodeIndex == rewriteEvent.childNodeIndex &&
            rewriteEventType == rewriteEvent.rewriteEventType &&
            Objects.equals(parentNode, rewriteEvent.parentNode) &&
            Objects.equals(oldChildNode, rewriteEvent.oldChildNode) &&
            Objects.equals(newChildNode, rewriteEvent.newChildNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rewriteEventType, parentNode, oldChildNode, newChildNode, childNodeIndex);
    }
}
