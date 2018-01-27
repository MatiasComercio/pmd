/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import java.util.List;

import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;
import net.sourceforge.pmd.lang.ast.Node;

// xnow document all
public class RewriteEventTranslatorImpl implements RewriteEventTranslator {
    private final NodeStringifier nodeStringifier;

    public RewriteEventTranslatorImpl(final NodeStringifier pNodeStringifier) {
        this.nodeStringifier = pNodeStringifier;
    }

    @Override
    public void translateToTextOperations(final RewriteEvent rewriteEvent, final List<String> textOperations) {
        switch (rewriteEvent.getRewriteEventType()) {
        case REMOVE:
            translateRemoveRewriteEventToTextOperation(rewriteEvent, textOperations);
            break;
        case INSERT:
            translateInsertRewriteEventToTextOperation(rewriteEvent, textOperations);
            break;
        case REPLACE:
            translateReplaceRewriteEventToTextOperation(rewriteEvent, textOperations);
            break;
        default:
            throw new IllegalStateException("Not a valid rewrite event.");
        }
    }

    // xnow document: For now, these methods are language agnostic, but may be easily customized per language,
    //  and that's why we have added to this interface
    public void translateRemoveRewriteEventToTextOperation(final RewriteEvent rewriteEvent, final List<String> textOperations) {
        // Get the text area of the node being removed and generate a text operation accordingly
        final Node node = rewriteEvent.getOldChildNode();
        // xnow: temporal implementation
        textOperations.add(String.format("REMOVE - beginLine: <%s>, beginColumn: <%s>, endLine: <%s>, endColumn: <%s>",
            node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn()));
    }

    public void translateInsertRewriteEventToTextOperation(final RewriteEvent rewriteEvent, final List<String> textOperations) {
        // Get the string representation of the newChildNode of the given rewrite event
        final String nodeAsString = nodeStringifier.stringify(rewriteEvent.getNewChildNode());
        // xnow: temporal implementation
        textOperations.add("INSERT - " + nodeAsString);
    }

    public void translateReplaceRewriteEventToTextOperation(final RewriteEvent rewriteEvent, final List<String> textOperations) {
        // Get the remove and insert text operations related to the given rewrite event
        // xnow: there is a replace event which takes the region to be deleted and the content of the new text to be
        // inserted; prefer that implementation over this one
        translateRemoveRewriteEventToTextOperation(rewriteEvent, textOperations);
        translateInsertRewriteEventToTextOperation(rewriteEvent, textOperations);
    }
}
