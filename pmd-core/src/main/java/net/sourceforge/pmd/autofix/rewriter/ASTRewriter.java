/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.VisitorStarter;
import net.sourceforge.pmd.lang.ast.Node;

// xnow document class & all methods
public class ASTRewriter { // xnow: implement interface
    private final LanguageVersionHandler languageVersionHandler;

    private ASTRewriter(final LanguageVersionHandler pLanguageVersionHandler) {
        this.languageVersionHandler = pLanguageVersionHandler;
    }

    public static ASTRewriter newInstance(final LanguageVersionHandler languageVersionHandler) {
        return new ASTRewriter(languageVersionHandler);
    }

    public List<String> getChildrenTextOperations(final Node node) {
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
                textOperations.addAll(getTextOperations(childRewriteEvent));
            }
        }
        return textOperations;
    }

    private VisitorStarter newNodeStringifier(final List<String> textOperations) {
        return languageVersionHandler.getNodeStringifier(textOperations);
    }

    private Collection<String> getTextOperations(final RewriteEvent childRewriteEvent) {
        final List<String> textOperations = new LinkedList<>();
        switch (childRewriteEvent.getRewriteEventType()) {
        case REMOVE:
            getRemoveTextOperation(childRewriteEvent, textOperations);
            break;
        case INSERT:
            getInsertTextOperation(childRewriteEvent, textOperations);
            break;
        case REPLACE:
            getReplaceTextOperation(childRewriteEvent, textOperations);
            break;
        default:
            throw new IllegalStateException("Not a valid rewrite event.");
        }
        return textOperations;
    }

    private void getRemoveTextOperation(final RewriteEvent childRewriteEvent,
                                        final List<String> textOperations) {
        final Node node = childRewriteEvent.getOldChildNode();
        /*
         * TODO: I think it is important to discuss whether remove text operations can be generic or are
         * language dependent. Initially, there would be no barriers in doing this in a generic way,
         * but custom format needs may arise, and then it would be nice to have a language-custom implementation.
         */
        textOperations.add(String.format("REMOVE - beginLine: <%s>, beginColumn: <%s>, endLine: <%s>, endColumn: <%s>",
            node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn()));
    }

    private void getInsertTextOperation(final RewriteEvent childRewriteEvent,
                                        final List<String> textOperations) {
        newNodeStringifier(textOperations).start(childRewriteEvent.getNewChildNode());
    }

    private void getReplaceTextOperation(final RewriteEvent childRewriteEvent,
                                         final List<String> textOperations) {
        getRemoveTextOperation(childRewriteEvent, textOperations);
        getInsertTextOperation(childRewriteEvent, textOperations);
    }
}
