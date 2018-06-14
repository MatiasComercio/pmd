/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class OutStates {
    private Set<State> set; // Not final as it may be reassigned as unmodifiable on `get` method call.
    private boolean locked;

    private OutStates(final Set<State> set, final boolean locked) {
        this.set = set;
        this.locked = locked;
    }

    public static OutStates newInstance() {
        return new OutStates(new HashSet<State>(), false);
    }

    public static OutStates newLockedInstance() {
        return new OutStates(Collections.<State>emptySet(), true);
    }

    public void add(final State outState) {
        if (!locked) {
            set.add(outState);
        }
    }

    public Set<State> get() {
        if (!locked) {
            set = Collections.unmodifiableSet(set);
        }
        return set;
    }
}
