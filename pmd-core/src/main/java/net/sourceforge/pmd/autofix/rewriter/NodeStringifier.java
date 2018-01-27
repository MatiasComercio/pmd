/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import net.sourceforge.pmd.lang.ast.Node;

// xnow document
public interface NodeStringifier {
    NodeStringifier DUMMY = new NodeStringifier() {
        @Override
        public String stringify(final Node node) {
            return ""; // does nothing - dummy implementation.
        }
    };

    String stringify(Node node);
}
