/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.value;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;

public final class TokenParsedValue implements ParsedValue {
    private final GenericToken token;
    private final TokenRegion tokenRegion;

    public TokenParsedValue(final GenericToken token) {
        this.token = token;
        this.tokenRegion = TokenRegion.newInstance(token, token);
    }

    @Override
    public boolean doesMatch(final ParsedValueMatcher matcher) {
        return matcher.doesMatch(token);
    }

    @Override
    public TokenRegion getTokenRegion() {
        return tokenRegion;
    }
}
