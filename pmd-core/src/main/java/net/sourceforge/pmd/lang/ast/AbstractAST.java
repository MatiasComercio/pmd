/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;


import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.autofix.rewriteevents.RewriteEventsRecorder;

public abstract class AbstractAST implements AST {
    private final RewriteEventsRecorder rewriteEventsRecorder;

    private RuleViolation ruleViolationBeingFixed;

    private AbstractAST() {
        this.rewriteEventsRecorder = new RewriteEventsRecorder();
    }

    @Override
    public void preFix(final RuleViolation ruleViolation) {
        ruleViolationBeingFixed = ruleViolation;
    }

    @Override
    public void postFix(final RuleViolation ruleViolation) {
        ruleViolationBeingFixed = null;
    }

    /*
        xnow: IDEAS
        - Can do the validation on the pre*Methods (the ones present at the rewrite events recorder)
        - Can generate the rewrite event directly here, and only call the recordRewriteEvent on the RewriteEventsRecorder: that makes MUCH more sense
            in terms of legibility and coherence among classes, and also makes it worth enough to have a pre&post for each rewrite events, fully validating all prior conditions
            and generating the actual events in the post actions.
     */
    @Override
    public void preInsertChild(final Node parent, final Node newChild, final int index) {
        // Nothing for now
    }

    @Override
    public void postInsertChild(final Node parent, final Node newChild, final int index) {
        if (shouldRewriteEventsBeRecorded()) {
            rewriteEventsRecorder.recordInsert(parent, newChild, index, ruleViolationBeingFixed);
        }
    }

    @Override
    public void preReplaceChild(final Node parent, final Node oldChild, final Node newChild, final int index) {
        // Nothing for now
    }

    @Override
    public void postReplaceChild(final Node parent, final Node oldChild, final Node newChild, final int index) {
        if (shouldRewriteEventsBeRecorded()) {
            rewriteEventsRecorder.recordReplace(parent, oldChild, newChild, index, ruleViolationBeingFixed);
        }
    }

    @Override
    public void preRemoveChild(final Node parent, final Node oldChild, final int index) {
        // Nothing for now
    }

    @Override
    public void postRemoveChild(final Node parent, final Node oldChild, final int index) {
        if (shouldRewriteEventsBeRecorded()) {
            rewriteEventsRecorder.recordRemove(parent, oldChild, index, ruleViolationBeingFixed);
        }
    }

    private boolean shouldRewriteEventsBeRecorded() {
        return ruleViolationBeingFixed != null;
    }
}
