/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.fixes.syntax.structure.element;

import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.LBRACKET;
import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.RBRACKET;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.java.ast.Dimensionable;
import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;
import net.sourceforge.pmd.lang.syntax.structure.element.AbstractReentrantMatchingStateSE;
import net.sourceforge.pmd.lang.syntax.structure.element.StructureElement;
import net.sourceforge.pmd.util.GenericTokens;

public class ArraySE extends AbstractReentrantMatchingStateSE {
    private static final int TOKENS_PER_DEPTH = 2; // Recall that: 2 tokens = "[" "]" = 1 array depth.

    private ArraySE() { }

    public static ArraySE newInstance() {
        return new ArraySE();
    }

    @Override
    public boolean shouldMatch(final ParsePolicy policy, final ParsedValue value) {
        return policy == ParsePolicy.COMPLETE && value != null;
    }

    @Override
    public boolean doesMatch(final GenericToken parsedToken) {
        return LBRACKET.getImage().equals(parsedToken.getImage()) || RBRACKET.getImage().equals(parsedToken.getImage());
    }

    @Override
    public boolean doesMatch(final AbstractNode parsedChild) {
        return false;
    }

    @Override
    public TokenRegion sync(final SyncContext syncContext,
                            final StructureElement oldSE,
                            final TokenRegion oldTokenRegion) {
        final AbstractNode node = syncContext.getNode();
        if (!(node instanceof Dimensionable)) {
            throw new IllegalStateException("ArraySE over a Non-Dimensionable node. Check NodesSyntax class.");
        }
        final Dimensionable dNode = (Dimensionable) node;
        return equals(oldSE) ? newReusableTokenRegion(dNode, oldTokenRegion) : newTokenRegion(dNode, oldTokenRegion);
    }

    private TokenRegion newReusableTokenRegion(final Dimensionable dNode, final TokenRegion oldArrayTokensRegion) {
        // We have granted that it is an array tokens region if here, so we only need to count tokens :D
        final int oldDepth = oldArrayTokensRegion.numTokens() / TOKENS_PER_DEPTH;
        final int newDepth = dNode.getArrayDepth();
        final TokenRegion newTokenRegion;
        if (oldDepth == newDepth) { // Depth hasn't changed => no changes needed.
            newTokenRegion = oldArrayTokensRegion;
        } else if (oldDepth < newDepth) { // We need to add new brackets.
            newTokenRegion = addDepth(oldArrayTokensRegion, oldDepth, newDepth);
        } else { // oldDepth > newDepth => we need to remove brackets.
            newTokenRegion = removeDepth(oldArrayTokensRegion, oldDepth, newDepth);
        }

        return newTokenRegion;
    }

    private TokenRegion removeDepth(final TokenRegion oldArrayTokensRegion,
                                    // Left for method signature consistency (see `ArraySE#addDepth`)
                                    @SuppressWarnings("unused") final int oldDepth,
                                    final int newDepth) {
        if (newDepth == 0) {
            return TokenRegion.newEmptyInstance();
        }

        final GenericToken firstToken = oldArrayTokensRegion.getFirstToken();
        final GenericToken lastToken = oldArrayTokensRegion.getLastToken();
        final int stoppingCount = newDepth * TOKENS_PER_DEPTH;

        int count = 1; // We start counting the first token in order to get the newLastToken pointing to an RBRACKET.
        GenericToken newLastToken = firstToken; // Should be an LBRACKET.
        while (newLastToken != lastToken && count != stoppingCount) {
            count++;
            newLastToken = newLastToken.getNext();
        }

        if (count != stoppingCount) { // FIXME: remove this after debugging.
            throw new IllegalStateException("Stopped: No more tokens. Should have stopped with `count`. Check code");
        }

        // Remove the last brackets.
        newLastToken.setNext(lastToken.getNext());

        return TokenRegion.newInstance(firstToken, newLastToken);
    }

    private TokenRegion addDepth(final TokenRegion oldArrayTokensRegion, final int oldDepth, final int newDepth) {
        return incrementArrayDepth(
            oldArrayTokensRegion.getFirstToken(),
            oldArrayTokensRegion.getLastToken(),
            newDepth - oldDepth
        );
    }

    private TokenRegion newTokenRegion(final Dimensionable dNode, final TokenRegion oldNonArrayTokenRegion) {
        final GenericToken emptyToken = GenericTokens.newEmptyToken();
        final TokenRegion newTokenRegion = incrementArrayDepth(emptyToken, emptyToken, dNode.getArrayDepth());

        // Set the old region special tokens.
        final GenericToken specialTokens = oldNonArrayTokenRegion.getFirstToken().getSpecialToken();
        newTokenRegion.getFirstToken().setSpecialToken(specialTokens);

        return newTokenRegion; // May be an empty region but with special tokens :D
    }

    private TokenRegion incrementArrayDepth(GenericToken firstToken, GenericToken lastToken, final int extraDepth) {
        for (int currExtraDepth = 0; currExtraDepth < extraDepth; currExtraDepth++) {
            final GenericToken lbracket = LBRACKET.newToken();
            final GenericToken rbracket = RBRACKET.newToken();
            lbracket.setNext(rbracket);

            if (GenericTokens.isEmptyToken(firstToken)) {
                firstToken = lbracket;
            }

            if (!GenericTokens.isEmptyToken(lastToken)) {
                lastToken.setNext(lbracket);
            }

            lastToken = rbracket;
        }

        if (firstToken == null) {
            return TokenRegion.newEmptyInstance();
        }

        return TokenRegion.newInstance(firstToken, lastToken);
    }
}
