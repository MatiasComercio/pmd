/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

// xaf: update documentation
public class RewriteEvent {
    private final Set<RuleViolation> originatingRuleViolations;
    private final Node oldChildNode;
    // xaf: document that this is only internal mutable field, just to avoid new objects creation on `mergeWith` method
    private Node newChildNode;

    private RewriteEvent(final Builder builder) {
        this.originatingRuleViolations = new HashSet<>();
        this.originatingRuleViolations.add(builder.originatingRuleViolation);
        this.oldChildNode = builder.oldChildNode;
        this.newChildNode = builder.newChildNode;
    }

    public static Builder newBuilder(final RuleViolation originatingRuleViolation) {
        return new Builder(originatingRuleViolation);
    }

    // xaf: self is the original/old event; returns null if no new event is created form the merge of these two
    // (i.e., if merging them deletes both of them)
    // xaf: document that it has side effects (it modifies this instance/self)
    // xaf: document what does Incompatible merge means.
    public RewriteEvent mergeWith(final RewriteEvent newRewriteEvent) {
        if (!Objects.equals(newChildNode, newRewriteEvent.oldChildNode)) {
            throw new IllegalArgumentException("Incompatible merge: new child node of this rewrite event "
                + "MUST be equal to old child node of the new rewrite event");
        }
        if (Objects.equals(oldChildNode, newRewriteEvent.newChildNode)) {
            return null; // Cancelling rewrite events
        }
        originatingRuleViolations.addAll(newRewriteEvent.originatingRuleViolations);
        newChildNode = newRewriteEvent.newChildNode;
        return this;
    }

    public Node getOldChildNode() {
        return oldChildNode;
    }

    public Node getNewChildNode() {
        return newChildNode;
    }

    public static class Builder {
        private final RuleViolation originatingRuleViolation;
        private Node oldChildNode;
        private Node newChildNode;

        private Builder(final RuleViolation theOriginatingRuleViolation) {
            this.originatingRuleViolation = Objects.requireNonNull(theOriginatingRuleViolation);
        }

        public Builder oldChildNode(final Node theOldChildNode) {
            this.oldChildNode = theOldChildNode;
            return this;
        }

        public Builder newChildNode(final Node theNewChildNode) {
            this.newChildNode = theNewChildNode;
            return this;
        }

        public RewriteEvent build() {
            if (Objects.equals(oldChildNode, newChildNode)) {
                throw new IllegalArgumentException("Cannot build a rewrite event where both old and new child are equal");
            }
            return new RewriteEvent(this);
        }
    }
}
