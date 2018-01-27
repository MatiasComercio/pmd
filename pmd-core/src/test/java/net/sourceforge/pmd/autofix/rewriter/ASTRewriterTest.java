/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;
import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.Node;

public class ASTRewriterTest {
    private static final int NUM_CHILDREN = 3;
    private static final int NUM_GRANDCHILDREN = 2;
    private static final RewriteEventTranslator TRANSLATOR = new DummyRewriteEventTranslatorImpl();
    private static final ASTRewriter AST_REWRITER = ASTRewriter.newInstance(TRANSLATOR);

    private static class DummyRewriteEventTranslatorImpl implements RewriteEventTranslator {
        private static final NodeStringifier STRINGIFIER = new DummyNodeStringifier();

        @Override
        public void translateToTextOperations(final RewriteEvent rewriteEvent, final List<String> textOperations) {
            switch (rewriteEvent.getRewriteEventType()) {
            case INSERT:
                textOperations.add(String.format("INSERT: %s",
                    STRINGIFIER.stringify(rewriteEvent.getNewChildNode())));
                break;
            case REPLACE:
                textOperations.add(String.format("REPLACE: %s => %s",
                    STRINGIFIER.stringify(rewriteEvent.getOldChildNode()),
                    STRINGIFIER.stringify(rewriteEvent.getNewChildNode())));
                break;
            case REMOVE:
                textOperations.add(String.format("REMOVE: %s",
                    STRINGIFIER.stringify(rewriteEvent.getOldChildNode())));
                break;
            default:
                throw new IllegalStateException();
            }
        }
    }

    private static class DummyNodeStringifier implements NodeStringifier {

        @Override
        public String stringify(final Node node) {
            return String.valueOf(node.jjtGetId());
        }
    }

    private Node rootNode;
    private List<String> expectedTextOperations;

    @Before
    public void createASTWithRewriteOperations() {
        rootNode = DummyNode.newAST(NUM_CHILDREN, NUM_GRANDCHILDREN);
        expectedTextOperations = new LinkedList<>();
        final Node insertNode = DummyNode.newInstance(-2);
        final Node replaceNode = DummyNode.newInstance(-3);
        rootNode.insert(insertNode, 0); // Expect: "Insert: -2"
        // Expect: "Replace: 1 -> -3" as the first child (id 1) is being shifted due to the insert operation
        rootNode.replace(replaceNode, 1);
        // Expect: "Remove: 4" which is the index of the second child, that has been also shifted due to the insertion
        rootNode.remove(2);

        final RewriteEvent[] rewriteEvents = rootNode.getChildrenRewriteEvents();
        for (final RewriteEvent rewriteEvent : rewriteEvents) {
            if (rewriteEvent != null) {
                TRANSLATOR.translateToTextOperations(rewriteEvent, expectedTextOperations);
            }
        }
    }

    @Test
    public void testGetTextOperations() {
        assertEquals(expectedTextOperations, AST_REWRITER.getTextOperations(rootNode));
    }
}
