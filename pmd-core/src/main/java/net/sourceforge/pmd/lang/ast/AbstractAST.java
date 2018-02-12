/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.autofix.rewriteevents.RewriteEventsRecorder;
import net.sourceforge.pmd.autofix.rewriteevents.RewriteEventsRecorderImpl;

public abstract class AbstractAST implements AST {
    private RewriteEventsRecorder rewriteEventsRecorder;

    private AbstractAST() {
        this.rewriteEventsRecorder = new RewriteEventsRecorderImpl();
    }

    // TODO: we may save all the data in the pre and then, call post without parameters, so as to use the pre parameters

    @Override
    public void preInsertChild(final Node child, final int index) {
        // Nothing to do here
    }

    @Override
    public void postInsertChild(final Node parentNode, final Node newChildNode, final int childIndex) {
        // This may be outside this scope, just to avoid calling it when using the jjtAddChild (just call it when rewriting indeed)
        rewriteEventsRecorder.recordInsert(parentNode, newChildNode, childIndex); // TODO [autofix]
    }

    @Override
    public void preSetChild(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {

    }

    @Override
    public void postSetChild(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {
        // xxx: this could be either an insert or a replace, depending on the context of the set call
        rewriteEventsRecorder.recordReplace(parentNode, oldChildNode, newChildNode, childIndex);
    }

    @Override
    public void preRemoveChild(final Node parentNode, final Node oldChildNode, final int childIndex) {
        // Nothing to do here
    }

    @Override
    public void postRemoveChild(final Node parentNode, final Node oldChildNode, final int childIndex) {
        rewriteEventsRecorder.recordRemove(parentNode, oldChildNode, childIndex);
    }

    @Override
    public void preFix(final RuleViolation ruleViolation) {

    }

    @Override
    public void postFix(final RuleViolation ruleViolation) {

    }

    private void removeChildEvent(final Node parentNode, final Node oldChildNode, final int childIndex) {

    }

    private void insertChildEvent(final Node parentNode, final Node newChildNode, final int childIndex) {

    }

    private void replaceChildEvent(final Node parentNode, final Node oldChildNode,
                                   final Node newChildNode, final int childIndex) {

    }
}
