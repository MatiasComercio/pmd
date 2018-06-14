/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.context;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.syntax.parser.value.ChildParsedValue;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;

public class ChildrenParseContext extends AbstractParseContext {
    private ChildrenParseContext(final AbstractNode aNode) {
        super(aNode, ParsePolicy.CHILD_ONLY);
    }

    public static ChildrenParseContext newInstance(final AbstractNode aNode) {
        return new ChildrenParseContext(aNode);
    }

    @Override
    public boolean hasNext() {
        return nextChild != null;
    }

    @Override
    public ParsedValue parseNext() {
        return new ChildParsedValue(consumeChild());
    }
}
