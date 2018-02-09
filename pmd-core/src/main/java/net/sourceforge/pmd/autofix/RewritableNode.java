/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.lang.ast.AST;
import net.sourceforge.pmd.lang.ast.Node;

// xnow: document
public interface RewritableNode extends Node {
    /**
     * Remove the current node from its parent, if any.
     */
    void remove();

    /**
     * Remove the child node at the given index from the node's children
     * list, if any; if not, no changes are done.
     * @param index
     *          The index of the child to be removed
     */
    void removeChild(int index);

    /**
     * <p>
     *  Insert the given new child node using the given index.
     * </p>
     * <p>
     *  If the index is negative, no operation is performed.
     *  If it is non-negative, the insertion index is computed as:
     *  <pre>
     *    insertionIndex = index <= numChildren ? index : numChildren;
     *  </pre>
     *  so as to ensure that the current node has no empty spaces (i.e., null children) in its internal structure.
     * </p>
     * All existing nodes from index position on are right-shifted.
     *
     * @param newChild The node to be inserted, not null
     * @param index The position where to insert the new child node.
     * @return The insertion index where the node was definitely inserted;
     *          or a negative value if no operation have been performed.
     * @throws NullPointerException if {@code newChild} is null
     */
    int insertChild(Node newChild, int index);

    AST getAST(); // xnow: document
}
