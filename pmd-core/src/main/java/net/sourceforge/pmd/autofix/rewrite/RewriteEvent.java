/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

// xaf: update documentation
public abstract class RewriteEvent {
    private final Node oldChild;
    private final Node newChild;

    public RewriteEvent(final RuleViolation originatingRuleViolation) {
        this.oldChild = theOldChild;
        this.newChild = theNewChild;
    }
}
