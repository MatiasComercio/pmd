/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

//xaf: document
public enum RewriteEventFactory {
    INSTANCE;

    public RewriteEvent newInsertEvent(final RuleViolation originatingRuleViolation, final Node newChildNode) {
        return RewriteEvent.newBuilder(originatingRuleViolation).newChildNode(newChildNode).build();
    }

    public RewriteEvent newReplaceEvent(final RuleViolation originatingRuleViolation,
                                        final Node oldChildNode, final Node newChildNode) {
        return RewriteEvent.newBuilder(originatingRuleViolation).oldChildNode(oldChildNode).newChildNode(newChildNode).build();
    }

    public RewriteEvent newRemoveEvent(final RuleViolation originatingRuleViolation, final Node oldChildNode) {
        return RewriteEvent.newBuilder(originatingRuleViolation).oldChildNode(oldChildNode).build();
    }
}
