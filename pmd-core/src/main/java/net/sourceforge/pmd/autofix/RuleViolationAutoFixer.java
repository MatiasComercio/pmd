/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.RuleViolation;

public interface RuleViolationAutoFixer {
    void apply();
    RuleViolation getRuleViolation();
}
