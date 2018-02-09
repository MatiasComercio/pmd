/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

public interface RuleViolationAutoFixer {
    void apply(RewritableNode node);
}
