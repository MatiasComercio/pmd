/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.lang.ast.Node;

public interface RuleViolationAutoFixer {
    void apply(Node node, NodeFixer nodeFixer);
}
