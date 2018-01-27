/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriter;

import net.sourceforge.pmd.lang.VisitorStarter;
import net.sourceforge.pmd.lang.ast.Node;

// xnow document all
public abstract class AbstractNodeStringifier implements NodeStringifier {
    @Override
    public String stringify(final Node node) {
        final StringBuilder stringBuilder = new StringBuilder();
        final VisitorStarter visitorStarter = getNodeStringifierVisitor(stringBuilder);
        visitorStarter.start(node);
        return stringBuilder.toString();
    }

    protected abstract VisitorStarter getNodeStringifierVisitor(StringBuilder stringBuilder);
}
