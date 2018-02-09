/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
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

    @Override // xnow: TODO: should update the documentation so as not to reveal implementation behavior
    // There is no report among the given parameters; it can be said that the created violation is properly linked to
    //  the given ruleContext and the implementation is the only aware of defining properly for each case.
    public void addViolation(RuleContext ruleContext, Rule rule, Node node, String message, Object[] args) {

        String formattedMessage = cleanup(message, args);
        // and internally, assign it to the report & to the ruleViolationFixer
        ruleContext.addRuleViolation(createRuleViolation(rule, ruleContext, node, formattedMessage));
        // xnow: TODO: have to do the same for the below method
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
