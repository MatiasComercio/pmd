/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax;

import net.sourceforge.pmd.lang.ast.AbstractNode;

public interface NodesSyntax<T extends AbstractNode> {
    // Return the node syntax of the node with the given class. Null if not found.
    <C extends T> NodeSyntax<C> getNodeSyntax(Class<C> nodeClass);
}
