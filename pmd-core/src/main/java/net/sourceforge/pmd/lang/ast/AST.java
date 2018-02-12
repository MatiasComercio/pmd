/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;


import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.autofix.rewrite.RewriteEventFactory;
import net.sourceforge.pmd.autofix.rewrite.RewriteEventsRecorder;
import net.sourceforge.pmd.autofix.rewrite.RewriteRecordFactory;

// xnow: implementing; UPDATE ALL DOCUMENTATION
/**
 * Classes implementing this interface should be in charge
 * of managing the context of all the nodes of the current AST.
 */
public class AST {
    private final RewriteEventsRecorder rewriteEventsRecorder;

    private RuleViolation ruleViolationBeingFixed;

    private AST() {
        this.rewriteEventsRecorder = new RewriteEventsRecorder();
    }

    /*
     * The idea of this is to control when rewrite operations are going to generate rewrite events or not.
     * If there is a rule violation set (after the preFix), then rewrite events will be recorded.
     * If there is not (after the postFix), then rewrite events will not be recorded.
     */
    public void preFix(final RuleViolation ruleViolation) {
        ruleViolationBeingFixed = ruleViolation;
    }

    public void postFix(final RuleViolation ruleViolation) {
        ruleViolationBeingFixed = null;
    }

    /*
        xnow: IDEAS
        - Can do the validation on the pre*Methods (the ones present at the rewrite events recorder)
            - These validations are being carried out by the `AbstractNode` implementation, so there is no need to double check that
        - Can generate the rewrite event directly here, and only call the recordRewriteEvent on the RewriteEventsRecorder: that makes MUCH more sense
            in terms of legibility and coherence among classes, and also makes it worth enough to have a pre&post for each rewrite events, fully validating all prior conditions
            and generating the actual events in the post actions.
     */
    /*
        xnow: as this class is for internal usage only, all post call must have it equal pre call, but this is not validated;
        it is (INTERNAL) user responsibility only.
        Other possibility would be to create the rewrite event in the pre and then in the post record it,
        but this may need a synchronization in case of multiple calls to the pre method before a post is called.
        Currently, this is not a possibility, but it may happen in a near future, or depending on future call contexts.
     */

    public void preInsertChild(final Node parent, final Node newChild, final int index) {
        // Nothing for now
    }

    public void postInsertChild(final Node parent, final Node newChild, final int index) {
        if (shouldRewriteEventsBeRecorded()) {
            rewriteEventsRecorder.record(RewriteRecordFactory.INSTANCE.newInsertRecord(parent, newChild, index, ruleViolationBeingFixed));
        }
    }

    public void preReplaceChild(final Node parent, final Node oldChild, final Node newChild, final int index) {
        // Nothing for now
    }

    public void postReplaceChild(final Node parent, final Node oldChild, final Node newChild, final int index) {
        if (shouldRewriteEventsBeRecorded()) {
            rewriteEventsRecorder.record(RewriteRecordFactory.INSTANCE.newReplaceRecord(parent, oldChild, newChild, index, ruleViolationBeingFixed));
        }
    }

    public void preRemoveChild(final Node parent, final Node oldChild, final int index) {
        // Nothing for now
    }

    public void postRemoveChild(final Node parent, final Node oldChild, final int index) {
        if (shouldRewriteEventsBeRecorded()) {
            rewriteEventsRecorder.record(RewriteRecordFactory.INSTANCE.newRemoveRecord(parent, oldChild, index, ruleViolationBeingFixed));
        }

    }

    private boolean shouldRewriteEventsBeRecorded() {
        return ruleViolationBeingFixed != null;
    }




    /*
     * xaf: we should use a method to enable/disable rewrite methods, in order to avoid the direct usage of this methods
     * during a visiting where we don't expect the user to do so.
     */

    //    /** xaf: add if needed
//     *
//     * @return {@code true} if any of this node's children have been modified; {@code false} otherwise
//     */
//    boolean haveChildrenChanged(Node parent);
//
//    /**
//     *
//     * @return A copy of all the {@link RewriteEvent}s that occurred over this node's children (may be null).
//     */
//    RewriteEvent[] getChildrenRewriteEvents(Node parent);
}
