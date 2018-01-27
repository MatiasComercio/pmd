/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import net.sourceforge.pmd.lang.ast.Node;

import jdk.internal.joptsimple.internal.Strings;

// xnow document
public interface NodeStringifier {
    NodeStringifier DUMMY = new NodeStringifier() {
        @Override
        public String stringify(final Node node) {
            return Strings.EMPTY; // does nothing - dummy implementation.
        }
    };

    String stringify(Node node);
}
