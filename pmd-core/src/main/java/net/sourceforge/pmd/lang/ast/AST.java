/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

// xnow

import net.sourceforge.pmd.RuleViolation;

/**
 * Classes implementing this interface should be in charge
 * of managing the context of all the nodes of the current AST
 */
public interface AST {
    void preInsertChild(Node child, int index);
    void postInsertChild(Node child, int index);
    void preSetChild(Node child, int index);
    void postSetChild(Node child, int index);
    void preRemoveChild(int index);
    void postRemoveChild(int index);

    /*
     * The idea of this is to control when rewrite operations are going to generate rewrite events or not.
     * If there is a rule violation set (after the preFix), then rewrite events will be recorded.
     * If there is not (after the postFix), then rewrite events will not be recorded.
     */
    void preFix(RuleViolation ruleViolation);
    void postFix(RuleViolation ruleViolation);

    /*
     * TODO: we should use a method to enable/disable rewrite methods, in order to avoid the direct usage of this methods
     * during a visiting where we don't expect the user to do so.
     */
}
