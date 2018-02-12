/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

//xnow: document
public enum RewriteEventFactory {
    INSTANCE;

    public InsertEvent newInsertEvent(final RuleViolation originatingRuleViolation, final Node newChild) {
        return new InsertEvent(originatingRuleViolation, newChild);
    }

    public ReplaceEvent newReplaceEvent(final RuleViolation originatingRuleViolation, final Node oldChild, final Node newChild) {
        return new ReplaceEvent(originatingRuleViolation, oldChild, newChild);
    }

    public RemoveEvent newRemoveEvent(final RuleViolation originatingRuleViolation, final Node oldChild) {
        return new RemoveEvent(originatingRuleViolation, oldChild);
    }
}
