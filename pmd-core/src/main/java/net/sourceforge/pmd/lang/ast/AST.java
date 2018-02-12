/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import net.sourceforge.pmd.RuleViolation;

/**
 * Classes implementing this interface should be in charge
 * of managing the context of all the nodes of the current AST.
 */
// xnow
public interface AST {

    /*
     * The idea of this is to control when rewrite operations are going to generate rewrite events or not.
     * If there is a rule violation set (after the preFix), then rewrite events will be recorded.
     * If there is not (after the postFix), then rewrite events will not be recorded.
     */
    void preFix(RuleViolation ruleViolation);

    void postFix(RuleViolation ruleViolation);

    void preInsertChild(Node parent, Node newChild, int index);

    void postInsertChild(Node parent, Node newChild, int index);

    void preReplaceChild(Node parent, Node oldChild, Node newChild, int index);

    void postReplaceChild(Node parent, Node oldChild, Node newChild, int index);

    void preRemoveChild(Node parent, Node oldChild, int index);

    void postRemoveChild(Node parent, Node oldChild, int index);

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
