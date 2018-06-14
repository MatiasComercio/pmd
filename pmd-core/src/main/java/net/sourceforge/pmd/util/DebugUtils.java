/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.syntax.parser.states.State;

public class DebugUtils {
    private static final String DUMP_SYMBOL = "> ";
    private static final String SPLIT_SYMBOL = "+ ";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String ALREADY_EXPANDED = "...";
    private static final String SECTION_SPLITTER = "================================";

    public static void dump(final State state) {
        System.out.println();
        System.out.println(SECTION_SPLITTER);
        System.out.println("Dumping state...");
        final StringBuilder sb = new StringBuilder();
        final Set<State> expanded = new HashSet<>();
        internalDump(state, 0, expanded, sb);
        System.out.println(sb);
        System.out.println(SECTION_SPLITTER);
        System.out.println();
    }

    private static void internalDump(final State state, final int nestedLevel,
                                     final Set<State> expanded, final StringBuilder sb) {
        sb.append(state);

        final Set<State> outStates = state.getOutStates().get();
        if (outStates.isEmpty()) {
            return;
        }

        sb.append(DUMP_SYMBOL);

        if (expanded.contains(state)) {
            sb.append(ALREADY_EXPANDED);
            return; // Do not expand this state.
        }
        // Expand this state.
        expanded.add(state);
        if (outStates.size() > 1) {
            for (final State outState : outStates) {
                sb.append(NEW_LINE);
                for (int i = 0; i <= nestedLevel; i++) sb.append(SPLIT_SYMBOL);
                internalDump(outState, nestedLevel + 1, expanded, sb);
            }
        } else {
            for (final State outState : outStates) {
                internalDump(outState, nestedLevel, expanded, sb);
            }
        }
    }
}
