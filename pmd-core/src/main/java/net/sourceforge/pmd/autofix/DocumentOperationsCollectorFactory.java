/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.autofix.rewrite.RewriteEventTranslator;

public enum DocumentOperationsCollectorFactory {
    INSTANCE;

    public DocumentOperationsCollector newCollector(final RewriteEventTranslator rewriteEventTranslator) {
        return new DocumentOperationsCollector(rewriteEventTranslator);
    }
}
