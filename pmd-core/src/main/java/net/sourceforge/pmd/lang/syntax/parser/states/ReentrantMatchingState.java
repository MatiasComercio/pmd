/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

import java.util.Collections;
import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.StructureTranscription;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.SEInfo;
import net.sourceforge.pmd.lang.syntax.structure.Structure;
import net.sourceforge.pmd.lang.syntax.structure.element.StructureElement;

public class ReentrantMatchingState extends AbstractNonEndState implements MatchingState {
    private final StructureElement seMatcher;
    private final boolean isMandatory;

    private ReentrantMatchingState(final StructureElement seMatcher, final boolean isMandatory) {
        this.seMatcher = seMatcher;
        this.isMandatory = isMandatory;
    }

    public static ReentrantMatchingState newInstance(final StructureElement seMatcher, final boolean isMandatory) {
        return new ReentrantMatchingState(seMatcher, isMandatory);
    }

    @Override
    public Set<StructureTranscription> transcribe(final ParsePolicy policy,
                                                  final ParsedValue value,
                                                  final Structure.Builder structureBuilder) {
        SEInfo thisSEInfo = structureBuilder.peekLastSEInfo();
        // This is the first time we are entering at this state => always have it, even if token region is empty.
        if (thisSEInfo == null || !thisSEInfo.getSE().equals(seMatcher)) {
            thisSEInfo = SEInfo.newInstance(seMatcher, isMandatory);
            structureBuilder.append(thisSEInfo);
        }

        if (!seMatcher.shouldMatch(policy, value)) {
            return delegateTranscribe(policy, value, structureBuilder);
        }

        if (value == null) { // Epsilon transition.
            return Collections.singleton(StructureTranscription.newInstance(this, structureBuilder));
        }

        if (!seMatcher.doesMatch(value)) {
            return delegateTranscribe(policy, value, structureBuilder);
        }

        // Extend this modifier's region as we've just matched a new modifier.
        thisSEInfo.extendRegion(value.getTokenRegion());
        // Return ourselves for next transcription step as there may be more modifiers; if not, we'll delegate it.
        return Collections.singleton(StructureTranscription.newInstance(this, structureBuilder));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + hashCode() + ")" + " - " + seMatcher.toString();
    }
}
