/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.context;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.syntax.parser.value.ChildParsedValue;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.parser.value.TokenParsedValue;

public class CompleteParseContext extends AbstractParseContext {
    private CompleteParseContext(final AbstractNode aNode) {
        super(aNode, ParsePolicy.COMPLETE);
    }

    public static CompleteParseContext newInstance(final AbstractNode aNode) {
        return new CompleteParseContext(aNode);
    }

    @Override
    public boolean hasNext() {
        return hasNextToken();
    }

    @Override
    public ParsedValue parseNext() {
        if (!hasNextToken()) return null;

        // nextToken != null if here.

        final GenericToken nextChildFirstToken = hasNextChild() ? nextChild.jjtGetFirstToken() : null;

        final ParsedValue parsedValue;
        if (nextToken == nextChildFirstToken) { // Child section.
            parsedValue = new ChildParsedValue(nextChild);
            nextToken = nextChild.jjtGetLastToken().getNext(); // Move up to the token next to this child.
            consumeChild(); // Consume this already used child.
        } else { // Token section.
            parsedValue = new TokenParsedValue(consumeToken());
        }

        return parsedValue;
    }
}
