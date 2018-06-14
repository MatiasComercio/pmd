/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.grammar.operator;

import static java.util.Objects.requireNonNull;

import net.sourceforge.pmd.lang.syntax.parser.NFAFragment;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;
import net.sourceforge.pmd.lang.syntax.parser.states.SplitState;

public final class ZeroOrOneSym implements OperatorSym {
    private final Symbol symbol;

    private ZeroOrOneSym(final Symbol symbol) {
        this.symbol = symbol;
    }

    public static ZeroOrOneSym newInstance(final Symbol symbol) {
        return new ZeroOrOneSym(requireNonNull(symbol));
    }

    /** Group all this symbols as a sequence, i.e., it concatenates them. */
    public static ZeroOrOneSym newInstance(final Symbol... symbols) {
        return new ZeroOrOneSym(ConcatenationSym.newInstance(symbols));
    }

    @Override
    public NFAFragment toNFAFragment(final boolean areStatesMandatory) {
        /*
         *
         * s: this state.
         * f: our symbol's fragment.
         *
         *   new fragment
         *   -------------
         *   |           |
         *       --> f -- -->
         * -> s -|
         *       -------- -->
         */

        final SplitState thisState = SplitState.newInstance();

        // Connect this state with the symbol's fragment.
        // This fragment may or may not be present, so it's not mandatory.
        final NFAFragment fragment = symbol.toNFAFragment(false);
        thisState.addOutState(fragment.getStartState());

        // Create this fragment and add both ours & our symbol's fragments out states pointers.
        final NFAFragment thisFragment = new NFAFragment(thisState);
        thisFragment.addOutStates(thisState.getOutStates());
        thisFragment.addAllOutStatesPointers(fragment.getOutStatesPointers());

        // Return this fragment.
        return thisFragment;
    }
}
