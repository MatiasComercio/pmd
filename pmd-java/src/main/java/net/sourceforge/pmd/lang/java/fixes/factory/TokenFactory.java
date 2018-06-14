/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.fixes.factory;

import net.sourceforge.pmd.lang.java.ast.JavaParserConstants;
import net.sourceforge.pmd.lang.java.ast.Token;

public enum TokenFactory { // TODO: may be improved :D
    INSTANCE;
    private static final int SPACE_TOKEN_TYPE = 1; // Extracted from parsing & debugging a space

    public Token newToken(final int tokenType) {
        return Token.newToken(tokenType, imageFor(tokenType));
    }

    public Token newToken(final int tokenType, final String image) {
        return Token.newToken(tokenType, image);
    }

    public String imageFor(final int tokenType) {
        return JavaParserConstants.tokenImage[tokenType].replace("\"", "");
    }

    public Token space() {
        return Token.newToken(SPACE_TOKEN_TYPE, imageFor(SPACE_TOKEN_TYPE));
    }
}
