/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix;

import net.sourceforge.pmd.lang.VisitorStarter;
import net.sourceforge.pmd.lang.ast.Node;

// xaf: document
public abstract class NodeStringifier { // xnow: rename to AbstractNodeStringifier and crate interface NodeStringifier when more mature
    public static final NodeStringifier DUMMY = new NodeStringifier() {
        @Override
        public String stringify(final Node node) {
            return ""; // does nothing - dummy implementation
        }

        @Override
        protected VisitorStarter getNodeStringifierVisitor(final StringBuilder stringBuilder) {
            return null; // does nothing - dummy implementation
        }
    };

    public String stringify(final Node node) {
        final StringBuilder stringBuilder = new StringBuilder();
        final VisitorStarter visitorStarter = getNodeStringifierVisitor(stringBuilder);
        visitorStarter.start(node);
        return stringBuilder.toString();
    }

    protected abstract VisitorStarter getNodeStringifierVisitor(StringBuilder stringBuilder);
}
