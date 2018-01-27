/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;
//
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;
//import net.sourceforge.pmd.lang.DummyLanguageModule;
//import net.sourceforge.pmd.lang.LanguageVersionHandler;
//import net.sourceforge.pmd.lang.ast.DummyNode;
//import net.sourceforge.pmd.lang.ast.Node;
//
//public class ASTRewriterTest {
//    private static final LanguageVersionHandler DUMMY_LANGUAGE_VERSION_HANDLER = getDummyLanguageVersionHandler();
//    private static final ASTRewriter AST_REWRITER = ASTRewriter.newInstance(DUMMY_LANGUAGE_VERSION_HANDLER);
//
//    private static final int NUM_CHILDREN = 3;
//    private static final int NUM_GRANDCHILDREN = 3;
//
//    private Node rootNode;
//
//    // TODO: implement custom stringifier for dummy node
//    private static LanguageVersionHandler getDummyLanguageVersionHandler() {
//        return new DummyLanguageModule.Handler() {
//            @Override
//            public RewriteEventTranslator getRewriteEventTranslator(final RewriteEvent rewriteEvent, final List<String> textOperations) {
//                super.getRewriteEventTranslator(rewriteEvent, textOperations);
//            }
//        };
//    }
//
//    @Before
//    public void createASTWithRewriteOperations() {
//        rootNode = DummyNode.newAST(NUM_CHILDREN, NUM_GRANDCHILDREN);
//        final Node newDummyNode = DummyNode.newInstance();
//        rootNode.insert(newDummyNode, 0);
//        rootNode.replace(newDummyNode, 1);
//        rootNode.remove(2);
//    }
//
//    @Test
//    public void testGetTextOperations() {
//        System.out.println(AST_REWRITER.getTextOperations(rootNode));
//    }
//}
