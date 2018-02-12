/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import java.util.Objects;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

// xaf: document
// xnow
public abstract class RewriteRecord {
    private final Node parent;
    private final RewriteEvent rewriteEvent;
    private final int index;
    private final RuleViolation originatingRuleViolation;

    protected RewriteRecord(final Node theParent,
                            final RewriteEvent theRewriteEvent,
                            final int theIndex,
                            final RuleViolation theOriginatingRuleViolation) {
        this.parent = theParent;
        this.rewriteEvent = theRewriteEvent;
        this.index = theIndex;
        this.originatingRuleViolation = theOriginatingRuleViolation;
    }

    // xaf: should generate a translation from this to text operations/string representation

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RewriteRecord that = (RewriteRecord) o;
        return index == that.index
            && Objects.equals(parent, that.parent)
            && Objects.equals(rewriteEvent, that.rewriteEvent)
            && Objects.equals(originatingRuleViolation, that.originatingRuleViolation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, rewriteEvent, index, originatingRuleViolation);
    }

    @Override
    public String toString() {
        return "RewriteRecord{"
            + "parent=" + parent
            + ", rewriteEvent=" + rewriteEvent
            + ", index=" + index
            + ", originatingRuleViolation=" + originatingRuleViolation
            + '}';
    }
}
