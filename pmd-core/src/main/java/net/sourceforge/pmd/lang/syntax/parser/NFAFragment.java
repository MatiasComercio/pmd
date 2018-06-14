/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.states.EndState;
import net.sourceforge.pmd.lang.syntax.parser.states.OutStates;
import net.sourceforge.pmd.lang.syntax.parser.states.State;

public class NFAFragment {
    private final State startState;
    private final Set<OutStates> outStates;

    public NFAFragment(final State startState) {
        this.startState = startState;
        this.outStates = new HashSet<>();
    }

    public static NFAFragment newInstance(final State startState) {
        return new NFAFragment(startState);
    }

    /**
     * Close this fragment with an end state and return the start state of this fragment
     * (i.e., the start state of the nfa represented by this fragment).
     */
    public State close() {
        final EndState endState = EndState.getInstance();
        patchOutStatesPointers(endState);
        return startState;
    }

    public State getStartState() {
        return startState;
    }

    public Set<OutStates> getOutStatesPointers() {
        return outStates;
    }

    public void addOutStates(final OutStates outStates) {
        this.outStates.add(outStates);
    }

    public void addAllOutStatesPointers(final Set<OutStates> outStates) {
        this.outStates.addAll(outStates);
    }

    public void patchOutStatesPointers(final State outState) {
        for (final OutStates outStates : this.outStates) {
            outStates.add(outState);
        }
    }
}
