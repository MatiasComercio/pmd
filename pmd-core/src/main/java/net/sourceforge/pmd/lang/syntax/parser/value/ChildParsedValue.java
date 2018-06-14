/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.value;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;

public final class ChildParsedValue implements ParsedValue {
    private final AbstractNode child;
    private final TokenRegion tokenRegion;

    public ChildParsedValue(final AbstractNode child) {
        this.child = child;
        this.tokenRegion = TokenRegion.newInstance(child.jjtGetFirstToken(), child.jjtGetLastToken());
    }

    @Override
    public boolean doesMatch(final ParsedValueMatcher matcher) {
        return matcher.doesMatch(child);
    }

    @Override
    public TokenRegion getTokenRegion() {
        return tokenRegion;
    }
}
