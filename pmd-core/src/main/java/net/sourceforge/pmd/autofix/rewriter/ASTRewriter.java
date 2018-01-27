/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;
import net.sourceforge.pmd.lang.ast.Node;

// xnow document class & all methods
public class ASTRewriter { // xnow: implement interface
    private final RewriteEventTranslator rewriteEventTranslator;

    private ASTRewriter(final RewriteEventTranslator pRewriteEventTranslator) {
        this.rewriteEventTranslator = pRewriteEventTranslator;
    }

    public static ASTRewriter newInstance(final RewriteEventTranslator rewriteEventTranslator) {
        return new ASTRewriter(rewriteEventTranslator);
    }

    private List<String> getChildrenTextOperations(final Node node) {
        final List<String> textOperations = new LinkedList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            textOperations.addAll(getTextOperations(node.jjtGetChild(i)));
        }
        return textOperations;
    }

    public List<String> getTextOperations(final Node node) {
        if (!node.haveChildrenChanged()) {
            // As the children of this node have not change themselves,
            // we keep digging for children's children changes
            return getChildrenTextOperations(node);
        }

        // At least one child has changed -> let's pick up all the text operations
        final List<String> textOperations = new LinkedList<>();
        final RewriteEvent[] childrenRewriteEvents = node.getChildrenRewriteEvents();
        for (int i = 0; i < childrenRewriteEvents.length; i++) {
            final RewriteEvent childRewriteEvent = childrenRewriteEvents[i];
            if (childRewriteEvent == null) {
                // Child itself has not changed, but its own children may have changed.
                // Let's collect those changes then.
                final Node childNode = node.jjtGetChild(i);
                if (childNode != null) { // xnow: think in which conditions childNode may be null
                    textOperations.addAll(getTextOperations(childNode));
                }
            } else {
                // This child has changes => let's pick them as a text operation
                rewriteEventTranslator.translateToTextOperations(childRewriteEvent, textOperations);
            }
        }
        return textOperations;
    }
}
