/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

public class DummyNode extends AbstractNode {
    public DummyNode(int id) {
        super(id);
    }

    private DummyNode(int id, int beginLine, int endLine, int beginColumn, int endColumn) {
        super(id, beginLine, endLine, beginColumn, endColumn);
    }

    public static Node newInstance() {
        return new DummyNode(0);
    }

    public static Node newInstance(int id) {
        return new DummyNode(id);
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
     * <p>All nodes are given an id, starting from 0 (which represents the root node), and they are DFS incremented.</p>
     * <p>Following the previous example, nodes' ids would be assigned as follows:</p>
     * <pre>
     *           2
     *          /
     *         1-3
     *        / \
     *       /   4
     *      0
     *       \   6
     *        \ /
     *         5-7
     *          \
     *           8
     * </pre>
     * @param childrenPerLayer The number of children per layer.
     * @return The described AST.
     */
    public static Node newAST(final int... childrenPerLayer) {
        final int startingId = 0;
        final Node rootNode = newInstance(startingId);
        newAST(rootNode, 0, childrenPerLayer, startingId + 1);
        return rootNode;
    }

    private static int newAST(final Node parentNode, final int currentLayer, final int[] childrenPerLayer,
                              final int startingId) {
        int id = startingId;
        if (currentLayer >= childrenPerLayer.length) {
            return id;
        }

        for (int childI = 0; childI < childrenPerLayer[currentLayer]; childI++) {
            final Node childNode = newInstance(id++);
            id = newAST(childNode, currentLayer + 1, childrenPerLayer, id);
            addChild(parentNode, childNode);
        }
        return id;
    }

    private static void addChild(final Node parent, final Node child) {
        parent.jjtAddChild(child, parent.jjtGetNumChildren()); // Append child at the end
        child.jjtSetParent(parent);
    }
}
