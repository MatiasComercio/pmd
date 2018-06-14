/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure.element;

import net.sourceforge.pmd.lang.syntax.parser.NFAFragment;
import net.sourceforge.pmd.lang.syntax.parser.states.MatchingState;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValueMatcher;

public abstract class AbstractSE implements StructureElement, ParsedValueMatcher {
    protected AbstractSE() {
    }

    @Override
    public NFAFragment toNFAFragment(final boolean areStatesMandatory) {
        final MatchingState thisState = newMatchingState(areStatesMandatory);
        final NFAFragment thisFragment = NFAFragment.newInstance(thisState);
        thisFragment.addOutStates(thisState.getOutStates());
        return thisFragment;
    }

    @Override
    public boolean doesMatch(final ParsedValue parsedValue) {
        return parsedValue.doesMatch(this);
    }

    protected abstract MatchingState newMatchingState(final boolean areStatesMandatory);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + hashCode() + ")";
    }
}
