/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser;

import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.states.EndState;
import net.sourceforge.pmd.lang.syntax.parser.states.State;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.Structure;

public class StructureTranscription {
    private final State aState;
    private final Structure.Builder structureBuilder;

    private StructureTranscription(final State aState, final Structure.Builder structureBuilder) {
        this.aState = aState;
        this.structureBuilder = structureBuilder;
    }

    public static StructureTranscription newInstance(final State aState,
                                                     final Structure.Builder structureBuilder) {
        return new StructureTranscription(aState, structureBuilder);
    }

    public Set<StructureTranscription> step(final ParsePolicy policy,
                                            final ParsedValue value) {
        return aState.transcribe(policy, value, structureBuilder);
    }

    public boolean hasFinished() {
        return EndState.getInstance().equals(aState);
    }

    public Structure get() {
        return structureBuilder.build();
    }
}
