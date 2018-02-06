/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.AST;
import net.sourceforge.pmd.lang.ast.Node;

public abstract class AbstractRuleViolationAutoFixer implements RuleViolationAutoFixer {
    private final RuleViolation ruleViolation; // TODO: set this somehow in the constructor

    @Override
    public void apply() {
        // Get and unlink the node from the rule violation so as not to save the node forever
        final Node violationNode = ruleViolation.consumeNode();
        final AST ast = violationNode.getAST();
        ast.preFix(ruleViolation); // This will lead us to link a RewriteEvent with the RuleViolation that generated it
        apply(violationNode);
        ast.postFix(ruleViolation); // This will unset the current rule violation context being fixed
        ruleViolation.discardNode();
    }

    @Override
    public RuleViolation getRuleViolation() {
        return ruleViolation;
    }

    protected abstract void apply(Node node);
}
