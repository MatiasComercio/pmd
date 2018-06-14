/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.context;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;

@SuppressWarnings("WeakerAccess") // Not package-private as it may be extended outside this package.
public abstract class AbstractParseContext implements ParseContext {
    private final AbstractNode aNode;
    private final GenericToken lastExclusiveToken;
    private final ParsePolicy parsePolicy;

    protected GenericToken nextToken;
    protected AbstractNode nextChild;

    protected AbstractParseContext(final AbstractNode aNode, final ParsePolicy parsePolicy) {
        this.aNode = aNode;
        this.parsePolicy = parsePolicy;
        final GenericToken lastInclusiveToken = aNode.jjtGetLastToken();
        this.lastExclusiveToken = lastInclusiveToken == null ? null : lastInclusiveToken.getNext();
        this.nextToken = aNode.jjtGetFirstToken();
        this.nextChild = aNode.jjtGetNumChildren() > 0 ? aNode.requireAbstractNode(aNode.jjtGetChild(0)) : null;
    }

    @Override
    public ParsePolicy getParsePolicy() {
        return parsePolicy;
    }

    protected boolean hasNextChild() {
        return nextChild != null;
    }

    protected AbstractNode consumeChild() {
        if (!hasNextChild()) return null;
        final AbstractNode currChild = nextChild;
        final int nextChildIndex = nextChild.jjtGetChildIndex() + 1;
        if (nextChildIndex < aNode.jjtGetNumChildren()) {
            nextChild = aNode.requireAbstractNode(aNode.jjtGetChild(nextChildIndex));
        } else {
            nextChild = null;
        }
        return currChild;
    }

    public boolean hasNextToken() {
        return nextToken != lastExclusiveToken && nextToken != null;
    }

    protected GenericToken consumeToken() {
        if (!hasNextToken()) return null;
        final GenericToken currToken = nextToken;
        nextToken = nextToken.getNext();
        return currToken;
    }
}
