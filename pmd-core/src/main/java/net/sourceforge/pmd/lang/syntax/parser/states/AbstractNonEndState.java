/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.StructureTranscription;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.Structure;

public abstract class AbstractNonEndState implements State {
    private final OutStates outStates;
    private boolean alreadyDelegated;

    @SuppressWarnings("WeakerAccess") // Not package-private as it may be extended outside this package.
    protected AbstractNonEndState() {
        this.outStates = OutStates.newInstance();
        this.alreadyDelegated = false;
    }

    @Override
    public void addOutState(final State state) {
        outStates.add(state);
    }

    @Override
    public OutStates getOutStates () {
        return outStates; // Returning NON-unmodifiable collection on purpose.
    }

    /**
     * Delegate the transcribe operation to all resolvable state (MatchingState) that follow us, avoiding
     * a possible self-delegation. This removes infinite loops of delegation.
     */
    @SuppressWarnings("WeakerAccess") // Not package-private as it may be extended outside this package.
    protected Set<StructureTranscription> delegateTranscribe(final ParsePolicy policy,
                                                             final ParsedValue value,
                                                             final Structure.Builder structureBuilder) {
        if (alreadyDelegated) {
            return Collections.emptySet();
        }

        alreadyDelegated = true;

        final Set<StructureTranscription> nextTranscriptions = new HashSet<>();
        for (final State outState : outStates.get()) {
            nextTranscriptions.addAll(outState.transcribe(policy, value, structureBuilder.duplicate()));
        }

        alreadyDelegated = false;
        return nextTranscriptions;
    }

}
