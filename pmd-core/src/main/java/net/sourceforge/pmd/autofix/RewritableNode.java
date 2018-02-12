/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.lang.ast.AST;
import net.sourceforge.pmd.lang.ast.Node;

// xnow: document
// xnow: think: COULD IT BE ASTNode? :smile: more similar to Eclipse namespace even
public interface RewritableNode extends Node {
    /**
     * Remove the current node from its parent, if any.
     */
    void remove();

    /**
     * Remove the child node at the given index from the node's children
     * list, if any; if not, no changes are done.
     * @param index The index of the child to be removed
     * @throws IllegalArgumentException if {@code index} is negative
     *                                  or is equal or greater than {@link Node#jjtGetNumChildren()}
     */
    void removeChild(int index);

    /**
     * <p>Insert the given new child node using the given index.</p>
     * <p>All existing nodes from index position on, if any, are right-shifted.</p>
     *
     * @param newChild The node to be inserted, not null
     * @param index The position where to insert the new child node.
     * @throws NullPointerException if {@code newChild} is null
     * @throws IllegalArgumentException if {@code index} is negative
     */
    void insertChild(Node newChild, int index);

    AST getAST(); // xnow: document
}
