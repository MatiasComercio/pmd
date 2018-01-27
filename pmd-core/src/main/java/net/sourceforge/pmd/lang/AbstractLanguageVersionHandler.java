/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

import java.io.Writer;
import java.util.List;

import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.dfa.DFAGraphRule;

/**
 * This is a generic implementation of the LanguageVersionHandler interface.
 *
 * @see LanguageVersionHandler
 */
public abstract class AbstractLanguageVersionHandler implements LanguageVersionHandler {

    @Override
    public DataFlowHandler getDataFlowHandler() {
        return DataFlowHandler.DUMMY;
    }

    @Override
    public XPathHandler getXPathHandler() {
        return XPathHandler.DUMMY;
    }

    @Override
    public ParserOptions getDefaultParserOptions() {
        return new ParserOptions();
    }

    @Override
    public VisitorStarter getDataFlowFacade() {
        return VisitorStarter.DUMMY;
    }

    @Override
    public VisitorStarter getSymbolFacade() {
        return VisitorStarter.DUMMY;
    }

    @Override
    public VisitorStarter getSymbolFacade(ClassLoader classLoader) {
        return VisitorStarter.DUMMY;
    }

    @Override
    public VisitorStarter getTypeResolutionFacade(ClassLoader classLoader) {
        return VisitorStarter.DUMMY;
    }

    @Override
    public VisitorStarter getDumpFacade(final Writer writer, final String prefix, final boolean recurse) {
        return VisitorStarter.DUMMY;
    }

    @Override
    public VisitorStarter getMultifileFacade() {
        return VisitorStarter.DUMMY;
    }

    @Override
    public DFAGraphRule getDFAGraphRule() {
        return null;
    }

    @Override
    public void translateRewriteEventToTextOperations(final RewriteEvent rewriteEvent,
                                                      final List<String> textOperations) {
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

    @Override
    public VisitorStarter getNodeStringifier(final List<String> textOperations) {
        return VisitorStarter.DUMMY;
    }

    // For now, these methods are language agnostic, but may be easily customized per language,
    //  and that's why we have added to this interface
    public void translateRemoveRewriteEventToTextOperation(final RewriteEvent rewriteEvent,
                                                            final List<String> textOperations) {
        // Get the text area of the node being removed and generate a text operation accordingly
        final Node node = rewriteEvent.getOldChildNode();
        textOperations.add(String.format("REMOVE - beginLine: <%s>, beginColumn: <%s>, endLine: <%s>, endColumn: <%s>",
            node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn()));
    }

    public void translateInsertRewriteEventToTextOperation(final RewriteEvent rewriteEvent,
                                                            final List<String> textOperations) {
        // Get the string representation of the newChildNode of the given rewrite event
        final VisitorStarter nodeStringifier = getNodeStringifier(textOperations);
        nodeStringifier.start(rewriteEvent.getNewChildNode());
    }

    public void translateReplaceRewriteEventToTextOperation(final RewriteEvent rewriteEvent,
                                                             final List<String> textOperations) {
        // Get the remove and insert text operations related to the given rewrite event
        translateRemoveRewriteEventToTextOperation(rewriteEvent, textOperations);
        translateInsertRewriteEventToTextOperation(rewriteEvent, textOperations);
    }
}
