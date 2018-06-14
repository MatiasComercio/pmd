/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

import java.util.Collections;
import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.StructureTranscription;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.Structure;

public class EndState implements MatchingState {
    private static final EndState INSTANCE = new EndState();

    private final OutStates outStates;

    public EndState() {
        outStates = OutStates.newLockedInstance(); // We have no out states.
    }

    public static EndState getInstance() {
        return INSTANCE;
    }

    @Override
    public void addOutState(final State state) {
        // Nothing to add as we are the end state indeed (we have no out states).
    }

    @Override
    public OutStates getOutStates() {
        return outStates;
    }

    @Override
    public Set<StructureTranscription> transcribe(final ParsePolicy policy,
                                                  final ParsedValue value,
                                                  final Structure.Builder structureBuilder) {
        if (value == null) { // Epsilon transition.
            return Collections.singleton(StructureTranscription.newInstance(this, structureBuilder));
        }
        // We have no out states => no next states => no transcription to perform.
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
