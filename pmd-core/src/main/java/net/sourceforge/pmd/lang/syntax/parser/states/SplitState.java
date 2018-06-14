/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.StructureTranscription;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.Structure;

public class SplitState extends AbstractNonEndState {
    private SplitState() {}

    public static SplitState newInstance() {
        return new SplitState();
    }

    @Override
    public Set<StructureTranscription> transcribe(final ParsePolicy policy,
                                                     final ParsedValue value,
                                                     final Structure.Builder structureBuilder) {
        return delegateTranscribe(policy, value, structureBuilder);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + hashCode() + ")";
    }
}
