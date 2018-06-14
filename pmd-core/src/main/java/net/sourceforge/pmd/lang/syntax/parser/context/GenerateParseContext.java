/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.context;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;

public class GenerateParseContext extends AbstractParseContext {
    private GenerateParseContext(final AbstractNode aNode) {
        super(aNode, ParsePolicy.GENERATE);
    }

    public static GenerateParseContext newInstance(final AbstractNode aNode) {
        return new GenerateParseContext(aNode);
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public ParsedValue parseNext() {
        return null;
    }
}
