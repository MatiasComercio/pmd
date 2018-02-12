/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

//xnow: document
public enum RewriteRecordFactory {
    INSTANCE;


    /**
     * Create a new insert record with the given data.
     * @param parent   The parent node over which the modification is occurring.
     * @param newChild The new child node value being inserted.
     * @param index   The index of the child node being modified.
     * @param originatingRuleViolation The rule violation originating the rewrite event to be recorded.
     * @return A new {@link InsertRecord} created with the given data.
     */
    public InsertRecord newInsertRecord(final Node parent, final Node newChild, final int index,
                                        final RuleViolation originatingRuleViolation) {
        final RewriteEvent rewriteEvent = new InsertEvent(newChild);
        return new InsertRecord(parent, rewriteEvent, index, originatingRuleViolation);
    }

    /**
     * Create a new replace record with the given data.
     * @param parent   The parent node over which the modification is occurring.
     * @param oldChild The old child node value being replaced.
     * @param newChild The new child node value replacing the old child node value.
     * @param index   The index of the child node being modified.
     * @param originatingRuleViolation The rule violation originating the rewrite event to be recorded.
     * @return A new {@link ReplaceRecord} created with the given data.
     */
    public ReplaceRecord newReplaceRecord(final Node parent, final Node oldChild, final Node newChild, final int index,
                                          final RuleViolation originatingRuleViolation) {
        final RewriteEvent rewriteEvent = new ReplaceEvent(oldChild, newChild);
        return new ReplaceRecord(parent, rewriteEvent, index, originatingRuleViolation);
    }

    /**
     * Create a new remove record with the given data.
     * @param parent   The parent node over which the modification is occurring.
     * @param oldChild The old child node value being removed.
     * @param index   The index of the child node being modified.
     * @param originatingRuleViolation The rule violation originating the rewrite event to be recorded.
     * @return A new {@link RemoveRecord} created with the given data.
     */
    public RemoveRecord newRemoveRecord(final Node parent, final Node oldChild, final int index,
                                         final RuleViolation originatingRuleViolation) {
        final RewriteEvent rewriteEvent = new RemoveEvent(oldChild);
        return new RemoveRecord(parent, rewriteEvent, index, originatingRuleViolation);
    }
}
