/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure.element;

import net.sourceforge.pmd.lang.syntax.parser.states.MatchingState;
import net.sourceforge.pmd.lang.syntax.parser.states.ReentrantMatchingState;

public abstract class AbstractReentrantMatchingStateSE extends AbstractSE {
    @Override
    protected MatchingState newMatchingState(final boolean areStatesMandatory) {
        return ReentrantMatchingState.newInstance(this, areStatesMandatory);
    }
}
