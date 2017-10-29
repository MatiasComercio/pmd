package net.sourceforge.pmd.lang.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link AbstractNode}.
 */
public class AbstractNodeTest {

    private int id;
    private Node rootNode;
    private int numChildren;
    private int numGrandChildren;

    private int nextId() {
        return id ++;
    }

    private Node newDummyNode() {
        return new DummyNode(nextId());
    }

    private static void addChild(final Node parent, final Node child) {
        parent.jjtAddChild(child, parent.jjtGetNumChildren()); // Append child at the end
        child.jjtSetParent(parent);
    }

    @Before
    public void setUpSampleNodeTree() {
        id = 0;
        rootNode = newDummyNode();
        numChildren = 3;
        numGrandChildren = 3;

        for (int i = 0 ; i < numChildren ; i++) {
            final Node child = newDummyNode();
            for (int j = 0 ; j < numGrandChildren ; j++) {
                final Node grandChild = newDummyNode();
                addChild(child, grandChild);
            }
            addChild(rootNode, child);
        }
    }

    /**
     * Explicitly tests the {@code remove} method, and implicitly the {@code removeChildAtIndex} method
     */
    @Test
    public void testRemoveSecondChildOfRootNode() {
        final Node secondChild = rootNode.jjtGetChild(1);

        // Check that the child has the expected properties
        assertEquals(numChildren, rootNode.jjtGetNumChildren());
        assertEquals(rootNode, secondChild.jjtGetParent());
        assertEquals(numGrandChildren, secondChild.jjtGetNumChildren());
        final Node[] secondChildChildren = new Node[secondChild.jjtGetNumChildren()];
        for (int i = 0; i < secondChildChildren.length; i++) {
            final Node secondChildChild = secondChild.jjtGetChild(i);
            secondChildChildren[i] = secondChildChild;
            assertEquals(secondChild, secondChildChild.jjtGetParent());
        }

        // Do the actual removal
        secondChild.remove();

        // Check that conditions have been successfully changed
        assertEquals(numChildren - 1, rootNode.jjtGetNumChildren());
        assertNull(secondChild.jjtGetParent());
        assertEquals(0, secondChild.jjtGetNumChildren());
        for (final Node aSecondChildChildren : secondChildChildren) {
            assertNull(aSecondChildChildren.jjtGetParent());
        }
    }

    /**
     * Explicitly tests the {@code remove} method, and implicitly the {@code removeChildAtIndex} method.
     * This is a border case as the root node does not have any parent.
     */
    @Test
    public void testRemoveRootNode() {
        // Check that the root node has the expected properties
        assertEquals(numChildren, rootNode.jjtGetNumChildren());
        assertNull(rootNode.jjtGetParent());
        final Node[] children = new Node[rootNode.jjtGetNumChildren()];
        for (int i = 0; i < children.length; i++) {
            final Node child = rootNode.jjtGetChild(i);
            children[i] = child;
            assertEquals(rootNode, child.jjtGetParent());
        }

        // Do the actual removal
        rootNode.remove();

        // Check that conditions have been successfully changed
        assertEquals(0, rootNode.jjtGetNumChildren());
        assertNull(rootNode.jjtGetParent());
        for (final Node aChild : children) {
            assertNull(aChild.jjtGetParent());
        }
    }

    /**
     * Explicitly tests the {@code remove} method, and implicitly the {@code removeChildAtIndex} method.
     * This is a border case as the grand child node does not have any children.
     */
    @Test
    public void testRemoveGrandChildNode() {
        final Node child = rootNode.jjtGetChild(1);
        final Node grandChild = child.jjtGetChild(1);

        // Check that the child has the expected properties
        assertEquals(numGrandChildren, child.jjtGetNumChildren());
        assertEquals(0, grandChild.jjtGetNumChildren());
        assertEquals(child, grandChild.jjtGetParent());

        // Do the actual removal
        grandChild.remove();

        // Check that conditions have been successfully changed
        assertEquals(numGrandChildren - 1, child.jjtGetNumChildren());
        assertEquals(0, grandChild.jjtGetNumChildren());
        assertNull(grandChild.jjtGetParent());
    }

    /**
     * Explicitly tests the {@code removeChildAtIndex} method.
     * This is a border case as the grand child node does not have any children.
     */
    @Test
    public void testRemoveRootNodeChildAtIndex1() {
        final Node[] originalChildren = new Node[rootNode.jjtGetNumChildren()];
        // Check that prior conditions are OK
        for (int i = 0 ; i < originalChildren.length ; i++) {
            originalChildren[i] = rootNode.jjtGetChild(i);
            assertEquals(i, originalChildren[i].jjtGetChildIndex());
            if (i > 0) {
                assertNotEquals(originalChildren[i-1], originalChildren[i]);
            }
        }
        assertEquals(numChildren, rootNode.jjtGetNumChildren());

        // Do the actual removal
        rootNode.removeChildAtIndex(1);

        // Check that conditions have been successfully changed
        assertEquals(numChildren - 1, rootNode.jjtGetNumChildren());
        for (int i = 0, j = 0 ; i < rootNode.jjtGetNumChildren() ; i++, j++) {
            if (j == 1) { // Skip the removed child
                j++;
            }
            // Check that the nodes have been rightly shifted
            assertEquals(originalChildren[j], rootNode.jjtGetChild(i));
            // Check that the child index has been updated
            assertEquals(i, rootNode.jjtGetChild(i).jjtGetChildIndex());
        }
    }
}
