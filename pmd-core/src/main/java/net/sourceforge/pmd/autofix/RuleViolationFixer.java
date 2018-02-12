/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import java.util.LinkedList;
import java.util.Queue;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.benchmark.Benchmark;
import net.sourceforge.pmd.benchmark.Benchmarker;
import net.sourceforge.pmd.lang.ast.AST;
import net.sourceforge.pmd.lang.ast.Node;

public class RuleViolationFixer {
    // xnow
    private static class RuleViolationFixData {
        private final RuleViolation ruleViolation;
        private final RuleViolationAutoFixer ruleViolationFix;
        private RewritableNode rewritableNode;

        private RuleViolationFixData(final RuleViolation pRuleViolation,
                                     final RuleViolationAutoFixer pRuleViolationAutoFixer,
                                     final RewritableNode pNode) {
            this.ruleViolation = pRuleViolation;
            this.ruleViolationFix = pRuleViolationAutoFixer;
            this.rewritableNode = pNode;
        }

        private RewritableNode consumeNode() {
            final RewritableNode auxNode = rewritableNode;
            this.rewritableNode = null;
            return auxNode;
        }
    }

    private RuleViolation lastReportedRuleViolation;
    private Queue<RuleViolationFixData> ruleViolationFixesData = new LinkedList<>();

    public void addRuleViolation(final RuleViolation ruleViolation) {
        lastReportedRuleViolation = ruleViolation;
    }

    public void addRuleViolationFix(final RuleViolationAutoFixer ruleViolationFix,
                                    final Node node) {
        // Do not apply fixes of suppressed rules
        // Do not apply fixes if the node is not a RewritableNode
        if (lastReportedRuleViolation == null || lastReportedRuleViolation.isSuppressed() || !(node instanceof RewritableNode)) {
            return; // Discard this fixer as there is no rule violation saved
        }

        ruleViolationFixesData.offer(new RuleViolationFixData(lastReportedRuleViolation,
            ruleViolationFix, (RewritableNode) node));
        lastReportedRuleViolation = null; // So as to clear the context for the next call to this method
    }

    public void applyRuleViolationFixes() {
        // xnow: TODO: add debug logging
        long start = System.currentTimeMillis();
        long end;
        while (!ruleViolationFixesData.isEmpty()) {
            final RuleViolationFixData ruleViolationFixData = ruleViolationFixesData.poll();
            // xnow: TODO: in a future, it will be possible to call a custom fixRuleViolation per language, perhaps
            //  to correctly set the context of changes (scopes, type references, and so on)
            fixRuleViolation(ruleViolationFixData);
            end = System.nanoTime();
            // xnow: implement proper ruleViolationFixData.toString
            Benchmarker.mark(Benchmark.RuleViolationFix, ruleViolationFixData.toString(), end - start, 1);
            start = end;
        }
    }

    private void fixRuleViolation(final RuleViolationFixData ruleViolationFixData) {
        // Get and unlink the node from the rule violation so as not to save the node forever
        final RuleViolation ruleViolation = ruleViolationFixData.ruleViolation;
        final RewritableNode violationNode = ruleViolationFixData.consumeNode();
        final AST ast = violationNode.getAST();
        ast.preFix(ruleViolation); // This will lead us to link a RewriteEvent with the RuleViolation that generated it
        ruleViolationFixData.ruleViolationFix.apply(violationNode);
        ast.postFix(ruleViolation); // This will unset the current rule violation context being fixed
    }
}
