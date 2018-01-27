/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import java.util.List;

import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;

// xnow document
public interface RewriteEventTranslator {
    void translateToTextOperations(RewriteEvent rewriteEvent, List<String> textOperations);
}
