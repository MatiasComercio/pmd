/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

public class DummyNode extends AbstractNode {
    public DummyNode(int id) {
        super(id);
    }

    public static Node newInstance() {
        return new DummyNode(0);
    }

    @Override
    public String toString() {
        return "dummyNode";
    }

    /**
     * <p>Build a new AST with one root node and height {@code childrenPerLayer.length}.</p>
     *
     * <p>Note that the word `layer` is used instead of depth or level as the first layer
     * ({@code childrenPerLayer[0]}) has index 0, which does not matches neither
     * the depth definition (depth 0 corresponds to the root node) nor the level definition
     * (level 0 does not even exist).</p>
     *
     * <p>For this purpose, layer is depth-1. So, for instance, all the direct children of a root
     * node are in layer 0, grandchildren of the root node are in layer 1 and so on.</p>
     *
     * <p> For example, if {@code childrenPerLayer} is [2,3], the created AST will have 1 root node with 2 children
     * and 3 grandchildren, i.e., it will have the following structure:</p>
     * <pre>
     *           o
     *          /
     *         o-o
     *        / \
     *       /   o
     *      o
     *       \   o
     *        \ /
     *         o-o
     *          \
     *           o
     * </pre>
     *
     * @param childrenPerLayer The number of children per layer.
     * @return The described AST.
     */
    public static Node newAST(final int... childrenPerLayer) {
        final Node rootNode = newInstance();
        newAST(rootNode, 0, childrenPerLayer);
        return rootNode;
    }

    private static void newAST(final Node parentNode, final int currentLayer, final int[] childrenPerLayer) {
        if (currentLayer >= childrenPerLayer.length) {
            return;
        }

        for (int childI = 0; childI < childrenPerLayer[currentLayer]; childI++) {
            final Node childNode = newInstance();
            newAST(childNode, currentLayer + 1, childrenPerLayer);
            addChild(parentNode, childNode);
        }
    }

    private static void addChild(final Node parent, final Node child) {
        parent.jjtAddChild(child, parent.jjtGetNumChildren()); // Append child at the end
        child.jjtSetParent(parent);
    }
}
