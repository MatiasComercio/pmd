/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.grammar.operator;

import net.sourceforge.pmd.lang.syntax.parser.NFAFragment;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;
import net.sourceforge.pmd.lang.syntax.parser.states.SplitState;

public final class AlternationSym implements OperatorSym {
    private final Symbol[] symbols;

    private AlternationSym(final Symbol[] symbols) {
        this.symbols = symbols;
    }

    public static AlternationSym newInstance(final Symbol... symbols) {
        if (symbols.length <= 0) {
            atLeastOneSEException();
        }
        return new AlternationSym(symbols);
    }

    @Override
    public NFAFragment toNFAFragment(final boolean areStatesMandatory) {
        /*
         *
         * s: this state.
         * f1: our first symbol's fragment.
         * f2: our second symbol's fragment.
         * ...
         * fn: our nth symbol's fragment.
         *
         *   new fragment
         *   -------------------
         *   |                 |
         *       |--> f1 ------ -->
         *       |--> f2 ------ -->
         * -> s -|
         *       |--> f(n-1) -- -->
         *       |--> fn ------ -->
         */

        final SplitState thisState = SplitState.newInstance();
        final NFAFragment thisFragment = NFAFragment.newInstance(thisState);

        /*
         * Add each of our symbols' fragments start states as our state's out states
         * & theirs outStatesPointers as our fragment's outStatesPointers.
         */
        for (final Symbol symbol : symbols) {
            /*
             * As it is a split => neither fragment is mandatory (one will be present and the other won't) =>
             *  => states inside any of these fragments are not mandatory.
             */
            final NFAFragment fragment = symbol.toNFAFragment(false);
            thisState.addOutState(fragment.getStartState());
            thisFragment.addAllOutStatesPointers(fragment.getOutStatesPointers());
        }

        return thisFragment;
    }

    private static void atLeastOneSEException() {
        throw new IllegalStateException("Concatenation should have at least one symbol inside. Aborting...");
    }
}
