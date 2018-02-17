/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.autofix.rewrite.RewriteEventTranslator;
import net.sourceforge.pmd.document.DocumentOperationsApplierForNonOverlappingRegions;
import net.sourceforge.pmd.lang.ast.Node;

// xnow: this should be an interface with necessary methods, and this class should be renamed to RewriteEventToDocumentOperationsCollector
public class DocumentOperationsCollector {
    private final RewriteEventTranslator rewriteEventTranslator;

    public DocumentOperationsCollector(final RewriteEventTranslator theRewriteEventTranslator) {
        this.rewriteEventTranslator = theRewriteEventTranslator;
    }

    public void collect(final DocumentOperationsApplierForNonOverlappingRegions applier, final Node rootNode) {
        // xnow: doing
    }
}
