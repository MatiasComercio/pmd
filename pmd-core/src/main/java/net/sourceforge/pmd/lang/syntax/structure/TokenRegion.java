/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure;

import static java.util.Objects.requireNonNull;
import static net.sourceforge.pmd.util.GenericTokens.newEmptyToken;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.util.GenericTokens;

public class TokenRegion {
    // Both tokens are INCLUSIVE.
    private final GenericToken firstToken;
    private final GenericToken lastToken;

    private TokenRegion(final GenericToken firstToken, final GenericToken lastToken) {
        this.firstToken = firstToken;
        this.lastToken = lastToken;
    }

    public static TokenRegion newInstance(final GenericToken firstToken, final GenericToken lastToken) {
        return new TokenRegion(requireNonNull(firstToken), requireNonNull(lastToken));
    }

    public static TokenRegion newEmptyInstance() {
        final GenericToken emptyToken = newEmptyToken();
        return newInstance(emptyToken, emptyToken);
    }

    public GenericToken getFirstToken() {
        return firstToken;
    }

    public GenericToken getLastToken() {
        return lastToken;
    }

    public String stringify() {
        return GenericTokens.stringify(firstToken, lastToken);
    }

    public boolean isEmpty() {
        return GenericTokens.isEmptyToken(firstToken) && GenericTokens.isEmptyToken(lastToken);
    }

    public int numTokens() {
        if (isEmpty()) return 0;

        GenericToken currToken = firstToken;
        int count = 0;
        while (currToken != lastToken) {
            count ++;
            currToken = currToken.getNext();
        }
        return ++ count; // increment & get because we need to count the last token too.
    }

    @Override
    public boolean equals(final Object obj) { // To explicitly specify that we want instance comparison.
        return super.equals(obj);
    }

    @Override
    public int hashCode() { // To explicitly specify that we want instance comparison.
        return super.hashCode();
    }
}
