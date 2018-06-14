/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.grammar.operator;

import static java.util.Objects.requireNonNull;

import net.sourceforge.pmd.lang.syntax.parser.NFAFragment;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;

public final class OneOrMoreSym implements OperatorSym {
    private final Symbol symbol;
    private OneOrMoreSym(final Symbol symbol) {
        /*
         *
         * s: this state.
         * f: our symbol's fragment.
         *
         *   new fragment
         *   --------------
         *   |            |
         *    |<----|
         * -> f --> s ---- -->
         *
         * To be able to represent this, but with different 'mandatory' flags,
         * we need to duplicate the f fragment into 2: one created with mandatory = true (the `one` of `OneOrMore`),
         * and then another one created with mandatory = false (the `more` of `OneOrMore`).
         *
         * With this correction, this fragment representations is:
         *
         * s: this state.
         * fN: our symbol's fragment (Not mandatory).
         * fM: our symbol's fragment (Mandatory).
         *
         *   new fragment
         *   ------------------
         *   |                |
         *           |<----|
         * -> fM --> s -- fN-- -->
         *
         * Note that this is the same as:
         * seq(fM, zeroOrMore(fN))
         *
         * So we end up translating this OneOrMore to that :D
         */
        this.symbol = ConcatenationSym.newInstance(symbol, ZeroOrMoreSym.newInstance(symbol));

    }

    public static OneOrMoreSym newInstance(final Symbol symbols) {
        return new OneOrMoreSym(requireNonNull(symbols));
    }

    /** Group all this symbols as a sequence, i.e., it concatenates them. */
    public static OneOrMoreSym newInstance(final Symbol... symbols) {
        return new OneOrMoreSym(ConcatenationSym.newInstance(symbols));
    }

    @Override
    public NFAFragment toNFAFragment(final boolean areStatesMandatory) {
        // Because of the translation stated at the constructor, this method is a simple forward :D
        return symbol.toNFAFragment(areStatesMandatory);
    }
}
