/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.StructureTranscription;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.Structure;

public interface State {
    void addOutState(final State state);

    OutStates getOutStates();

    Set<StructureTranscription> transcribe(ParsePolicy policy,
                                           ParsedValue value,
                                           Structure.Builder structureBuilder);
}
