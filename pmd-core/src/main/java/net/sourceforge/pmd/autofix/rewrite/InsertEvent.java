/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

public final class InsertEvent extends RewriteEvent {
    private final Node newChildNode;

    public InsertEvent(final RuleViolation originatingRuleViolation, final Node theNewChildNode) {
        super(originatingRuleViolation);
        this.newChildNode = theNewChildNode;
    }

    public RewriteEvent[] mergeWith(final RewriteEvent newRewriteEvent) {

    }
}
