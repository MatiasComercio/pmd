/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.grammar.operator;

import static java.util.Objects.requireNonNull;

import net.sourceforge.pmd.lang.syntax.parser.NFAFragment;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;

public final class ConcatenationSym implements OperatorSym {
    private final Symbol[] symbols;

    private ConcatenationSym(final Symbol[] symbols) {
        this.symbols = symbols;
    }

    public static ConcatenationSym newInstance(final Symbol... symbols) {
        if (symbols.length <= 0) {
            atLeastOneSymbolException();
        }
        return new ConcatenationSym(symbols);
    }

    @Override
    public NFAFragment toNFAFragment(final boolean areStatesMandatory) {
        /*
         *
         * f1: our first symbol's fragment.
         * f2: our second symbol's fragment.
         * ...
         * fn: our nth symbol's fragment.
         *
         *           new fragment
         *   -----------------------------
         *   |                           |
         * -> f1 --> f2 --> ... --> fn -- -->
         */

        NFAFragment firstFragment = null;
        NFAFragment prevFragment = null;
        for (final Symbol symbol : symbols) {
            // As it is a concatenation, there's no split added => we keep the same mandatory states value.
            final NFAFragment currFragment = requireNonNull(symbol).toNFAFragment(areStatesMandatory);
            if (firstFragment == null) {
                firstFragment = currFragment;
            }

            if (prevFragment != null) {
                prevFragment.patchOutStatesPointers(currFragment.getStartState());
            }
            prevFragment = currFragment;
        }

        // This SHOULD NOT happen, as because of instance construction validation, there should be at least one symbol.
        if (firstFragment == null) {
            atLeastOneSymbolException();
        }

        final NFAFragment thisFragment = NFAFragment.newInstance(firstFragment.getStartState());
        thisFragment.addAllOutStatesPointers(prevFragment.getOutStatesPointers());

        return thisFragment;
    }

    private static void atLeastOneSymbolException() {
        throw new IllegalStateException("Concatenation should have at least one symbol inside. Aborting...");
    }
}
