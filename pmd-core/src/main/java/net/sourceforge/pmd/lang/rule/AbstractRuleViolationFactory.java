/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.autofix.RuleViolationAutoFixer;
import net.sourceforge.pmd.lang.ast.Node;

public abstract class AbstractRuleViolationFactory implements RuleViolationFactory {

    private static final Object[] NO_ARGS = new Object[0];

    private String cleanup(String message, Object[] args) {

        if (message != null) {
            // Escape PMD specific variable message format, specifically the {
            // in the ${, so MessageFormat doesn't bitch.
            final String escapedMessage = StringUtils.replace(message, "${", "$'{'");
            return MessageFormat.format(escapedMessage, args != null ? args : NO_ARGS);
        } else {
            return message;
        }
    }

    @Override
    public void addViolation(RuleContext ruleContext, Rule rule, Node node, String message, Object[] args) {

        String formattedMessage = cleanup(message, args);

        final RuleViolation ruleViolation = createRuleViolation(rule, ruleContext, node, formattedMessage);
        // xnow: done here only as it is the same
        ruleContext.getReport().addRuleViolation(ruleViolation);
        ruleViolationFixer.setRuleViolation(ruleViolation); // TODO: we should enforce this to be a constructor parameter (perhaps in a builder or sth of that sort)
        ruleContext.add(ruleViolationFixer);

        // xnow: another possibility, which involves no API change here
        /*
         * This makes it more difficult to enforce the constructor parameter; perhaps this may be done inside the rule context, silently
         */
        ruleContext.peekRuleViolationFixer().setRuleViolation(ruleViolation);
        // xnow: another possibility
        ruleContext.added(ruleViolation); // and internally, assign it to the ruleViolationFixer
        // xnow: I prefer this more than the others as it let us link the rule violation with the rule violation fixer in any reported
        // order, i.e., we can first report the rule violation and then the fixer or the other way around
    }

    @Override
    public void addViolation(RuleContext ruleContext, Rule rule, Node node, String message, int beginLine, int endLine,
            Object[] args) {

        String formattedMessage = cleanup(message, args);

        ruleContext.getReport()
                .addRuleViolation(createRuleViolation(rule, ruleContext, node, formattedMessage, beginLine, endLine));
    }

    protected abstract RuleViolation createRuleViolation(Rule rule, RuleContext ruleContext, Node node, String message);

    protected abstract RuleViolation createRuleViolation(Rule rule, RuleContext ruleContext, Node node, String message,
            int beginLine, int endLine);
}
