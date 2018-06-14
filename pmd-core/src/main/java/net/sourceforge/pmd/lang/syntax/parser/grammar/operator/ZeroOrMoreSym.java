/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.grammar.operator;

import static java.util.Objects.requireNonNull;

import net.sourceforge.pmd.lang.syntax.parser.NFAFragment;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;
import net.sourceforge.pmd.lang.syntax.parser.states.SplitState;

public final class ZeroOrMoreSym implements OperatorSym {
    private final Symbol structureElement;

    private ZeroOrMoreSym(final Symbol symbol) {
        this.structureElement = symbol;
    }

    public static ZeroOrMoreSym newInstance(final Symbol symbol) {
        return new ZeroOrMoreSym(requireNonNull(symbol));
    }

    /** Group all this symbols as a sequence, i.e., it concatenates them. */
    public static ZeroOrMoreSym newInstance(final Symbol... symbols) {
        return new ZeroOrMoreSym(ConcatenationSym.newInstance(symbols));
    }

    @Override
    public NFAFragment toNFAFragment(final boolean areStatesMandatory) {
        /*
         *
         * s: this state.
         * f: our symbol's fragment.
         *
         *   new fragment
         *   ------------
         *   |          |
         *    |--> f --|
         * -> s <------|
         *    |
         *    |---------- -->
         */

        final SplitState thisState = SplitState.newInstance();

        // Connect this state with the symbol fragment.
        // This fragment may or may not be present, so it's not mandatory.
        final NFAFragment fragment = structureElement.toNFAFragment(false);
        thisState.addOutState(fragment.getStartState());

        // Connect all the symbol fragment out state pointers to us
        fragment.patchOutStatesPointers(thisState);

        // Create this fragment and add ours out states pointers.
        final NFAFragment thisFragment = NFAFragment.newInstance(thisState);
        thisFragment.addOutStates(thisState.getOutStates());

        // Return this fragment.
        return thisFragment;
    }
}
