/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.StructureTranscription;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.SEInfo;
import net.sourceforge.pmd.lang.syntax.structure.Structure;
import net.sourceforge.pmd.lang.syntax.structure.element.StructureElement;

public class SingleMatchingState extends AbstractNonEndState implements MatchingState {
    private final StructureElement seMatcher;
    private final boolean isMandatory;

    private SingleMatchingState(final StructureElement seMatcher, final boolean isMandatory) {
        this.seMatcher = seMatcher;
        this.isMandatory = isMandatory;
    }

    public static SingleMatchingState newInstance(final StructureElement seMatcher, final boolean isMandatory) {
        return new SingleMatchingState(requireNonNull(seMatcher), isMandatory);
    }

    @Override
    public Set<StructureTranscription> transcribe(final ParsePolicy policy,
                                                  final ParsedValue value,
                                                  final Structure.Builder structureBuilder) {
        if (!seMatcher.shouldMatch(policy, value)) {
            structureBuilder.append(SEInfo.newInstance(seMatcher, isMandatory));
            return delegateTranscribe(policy, value, structureBuilder);
        }

        /*
         * null value is like an epsilon transition.
         * As we should match this concrete value before proceeding (according to the given policy),
         * we return ourselves as the next state (i.e., we have no epsilon transition, so we stay in our state).
         */
        if (value == null) {
            return Collections.singleton(StructureTranscription.newInstance(this, structureBuilder));
        }

        if (!seMatcher.doesMatch(value)) {
            return Collections.emptySet();
        }

        /*
         * We match =>
         *      => we add our seMatcher to the structure being built
         *      & return all our next states with this structure.
         */

        final Set<State> outStates = getOutStates().get();
        if (outStates.isEmpty()) {
            return Collections.emptySet();
        }

        structureBuilder.append(SEInfo.newInstance(seMatcher, isMandatory, value.getTokenRegion()));

        final Set<StructureTranscription> nextStates = new HashSet<>(outStates.size());
        for (final State outState : outStates) {
            nextStates.add(StructureTranscription.newInstance(outState, structureBuilder.duplicate()));
        }

        return nextStates;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + hashCode() + ")" + " - " + seMatcher.toString();
    }
}
