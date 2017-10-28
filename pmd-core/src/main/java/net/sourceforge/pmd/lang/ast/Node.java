/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. Node.java */

package net.sourceforge.pmd.lang.ast;

import java.util.List;

import org.jaxen.JaxenException;
import org.w3c.dom.Document;

import net.sourceforge.pmd.lang.dfa.DataFlowNode;

/**
 * All AST nodes must implement this interface. It provides basic
 * machinery for constructing the parent and child relationships
 * between nodes.
 */
public interface Node {

    /**
     * This method is called after the node has been made the current node. It
     * indicates that child nodes can now be added to it.
     */
    void jjtOpen();

    /**
     * This method is called after all the child nodes have been added.
     */
    void jjtClose();


    /**
     * Sets the parent of this node.
     *
     * @param parent The parent
     */
    void jjtSetParent(Node parent);


    /**
     * Returns the parent of this node.
     *
     * @return The parent of the node
     */
    Node jjtGetParent();


    /**
     * This method tells the node to add its argument to the node's list of
     * children.
     *
     * @param child The child to add
     * @param index The index to which the child will be added
     */
    void jjtAddChild(Node child, int index);

    /**
     * Sets the index of this node from the perspective of its parent. This
     * means: this.jjtGetParent().jjtGetChild(index) == this.
     *
     * @param index
     *            the child index
     */
    void jjtSetChildIndex(int index);


    /**
     * Gets the index of this node in the children of its parent.
     *
     * @return The index of the node
     */
    int jjtGetChildIndex();

    /**
     * This method returns a child node. The children are numbered from zero,
     * left to right.
     *
     * @param index
     *            the child index. Must be nonnegative and less than
     *            {@link #jjtGetNumChildren}.
     */
    Node jjtGetChild(int index);

    /**
     * Return the number of children the node has.
     */
    int jjtGetNumChildren();

    int jjtGetId();

    String getImage();

    void setImage(String image);

    boolean hasImageEqualTo(String image);

    int getBeginLine();

    int getBeginColumn();

    int getEndLine();

    int getEndColumn();

    DataFlowNode getDataFlowNode();

    void setDataFlowNode(DataFlowNode dataFlowNode);

    boolean isFindBoundary();


    /**
     * Returns the n-th parent or null if there are not {@code n} ancestors
     *
     * @param n how many ancestors to iterate over.
     *
     * @return the n-th parent or null.
     *
     * @throws IllegalArgumentException if {@code n} is negative or zero.
     */
    Node getNthParent(int n);


    /**
     * Traverses up the tree to find the first parent instance of type
     * parentType or one of its subclasses.
     *
     * @param parentType Class literal of the type you want to find
     * @param <T>        The type you want to find
     *
     * @return Node of type parentType. Returns null if none found.
     */
    <T> T getFirstParentOfType(Class<T> parentType);


    /**
     * Traverses up the tree to find all of the parent instances of type
     * parentType or one of its subclasses.
     *
     * @param parentType Class literal of the type you want to find
     * @param <T>        The type you want to find
     *
     * @return List of parentType instances found.
     */
    <T> List<T> getParentsOfType(Class<T> parentType);

    /**
     * Traverses the children to find all the instances of type childType or
     * one of its subclasses.
     *
     * @see #findDescendantsOfType(Class) if traversal of the entire tree is
     *      needed.
     *
     * @param childType
     *            class which you want to find.
     * @return List of all children of type childType. Returns an empty list if
     *         none found.
     */
    <T> List<T> findChildrenOfType(Class<T> childType);

    /**
     * Traverses down the tree to find all the descendant instances of type
     * descendantType.
     *
     * @param targetType
     *            class which you want to find.
     * @return List of all children of type targetType. Returns an empty list if
     *         none found.
     */
    <T> List<T> findDescendantsOfType(Class<T> targetType);

    /**
     * Traverses down the tree to find all the descendant instances of type
     * descendantType.
     *
     * @param targetType
     *            class which you want to find.
     * @param results
     *            list to store the matching descendants
     * @param crossFindBoundaries
     *            if <code>false</code>, recursion stops for nodes for which
     *            {@link #isFindBoundary()} is <code>true</code>
     */
    <T> void findDescendantsOfType(Class<T> targetType, List<T> results, boolean crossFindBoundaries);

    /**
     * Traverses the children to find the first instance of type childType.
     *
     * @see #getFirstDescendantOfType(Class) if traversal of the entire tree is
     *      needed.
     *
     * @param childType
     *            class which you want to find.
     * @return Node of type childType. Returns <code>null</code> if none found.
     */
    <T> T getFirstChildOfType(Class<T> childType);

    /**
     * Traverses down the tree to find the first descendant instance of type
     * descendantType.
     *
     * @param descendantType
     *            class which you want to find.
     * @return Node of type descendantType. Returns <code>null</code> if none
     *         found.
     */
    <T> T getFirstDescendantOfType(Class<T> descendantType);

    /**
     * Finds if this node contains a descendant of the given type.
     *
     * @param type
     *            the node type to search
     * @return <code>true</code> if there is at least one descendant of the
     *         given type
     */
    <T> boolean hasDescendantOfType(Class<T> type);

    /**
     * Returns all the nodes matching the xpath expression.
     *
     * @param xpathString
     *            the expression to check
     * @return List of all matching nodes. Returns an empty list if none found.
     * @throws JaxenException if the xpath is incorrect or fails altogether
     */
    List<? extends Node> findChildNodesWithXPath(String xpathString) throws JaxenException;

    /**
     * Checks whether at least one descendant matches the xpath expression.
     *
     * @param xpathString
     *            the expression to check
     * @return true if there is a match
     */
    boolean hasDescendantMatchingXPath(String xpathString);

    /**
     * Get a DOM Document which contains Elements and Attributes representative
     * of this Node and it's children. Essentially a DOM tree representation of
     * the Node AST, thereby allowing tools which can operate upon DOM to also
     * indirectly operate on the AST.
     */
    Document getAsDocument();

    /**
     * Get the user data associated with this node. By default there is no data,
     * unless it has been set via {@link #setUserData(Object)}.
     *
     * @return The user data set on this node.
     */
    Object getUserData();

    /**
     * Set the user data associated with this node.
     * <p>
     * PMD itself will never set user data onto a node. Nor should any Rule
     * implementation, as the AST nodes are shared between concurrently
     * executing Rules (i.e. it is <strong>not</strong> thread-safe).
     * <p>
     * This API is most useful for external applications looking to leverage
     * PMD's robust support for AST structures, in which case application
     * specific annotations on the AST nodes can be quite useful.
     *
     * @param userData
     *            The data to set on this node.
     */
    void setUserData(Object userData);

    /**
     * Remove the current node from its parent & the association it has with all its children.
     * <p>
     * This last type of removal makes it possible to avoid the visitor to visit children of removed nodes.
     */
    void remove();

    /**
     * Remove the child at the given index, if any.
     * If not, no changes are done.
     * @param childIndex
     *          The index of the child to be removed
     */
    void removeChildAtIndex(final int childIndex);
}
