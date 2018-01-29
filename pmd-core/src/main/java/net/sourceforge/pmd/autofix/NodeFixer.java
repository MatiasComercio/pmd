/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.lang.ast.Node;

public interface NodeFixer {
    void insert(Node parentNode, Node childNode, int childIndex);
    void remove(Node parentNode, int childIndex);
    void replace(Node parentNode, Node childNode, int childIndex);
}
