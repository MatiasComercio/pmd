/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import net.sourceforge.pmd.autofix.NodeStringifier;

// xnow
public class RewriteEventTranslator {
    private final NodeStringifier nodeStringifier;

    public RewriteEventTranslator(final NodeStringifier theNodeStringifier) {
        this.nodeStringifier = theNodeStringifier;
    }
}
