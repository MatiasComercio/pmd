/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.syntax.parser.context.ChildrenParseContext;
import net.sourceforge.pmd.lang.syntax.parser.context.CompleteParseContext;
import net.sourceforge.pmd.lang.syntax.parser.context.GenerateParseContext;
import net.sourceforge.pmd.lang.syntax.parser.context.ParseContext;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;
import net.sourceforge.pmd.lang.syntax.parser.states.State;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.Structure;
import net.sourceforge.pmd.util.DebugUtils;

/*
 * Based on Thompson NFA.
 * Many many thanks to: https://swtch.com/~rsc/regexp/regexp1.html
 */
public class Parser {
    private final State startState;

    private Parser(final State startState) {
        this.startState = startState;
        DebugUtils.dump(startState);
    }

    /** Build new instance (and all its parser states) based on the given symbol. */
    public static Parser newInstance(final Symbol symbol) {
        return new Parser(compileNFA(symbol));
    }

    /** Return the structure of the new given node. */
    public Set<Structure> parseNew(final AbstractNode abstractNode) {
        final Set<Structure> structures = parse(GenerateParseContext.newInstance(abstractNode));
        validateNotEmpty(structures);
        return structures;
    }

    /** Return the structure of the given node based on its tokens & children. */
    public Structure parseComplete(final AbstractNode abstractNode) {
        final Set<Structure> structures = parse(CompleteParseContext.newInstance(abstractNode));
        validateNotEmpty(structures);
        if (structures.size() > 1) {
            throw new IllegalStateException("There should be only one matching structure for the given node.");
        }
        return structures.iterator().next();
    }

    private void validateNotEmpty(final Set<Structure> structures) {
        if (structures.isEmpty()) {
            throw new IllegalStateException("There is no matching structure for the given node.");
        }
    }

    /** Return all the possible structures of the given node based on its children only. */
    public Set<Structure> parseChildren(final AbstractNode abstractNode) {
        final Set<Structure> possibleStructures = parse(ChildrenParseContext.newInstance(abstractNode));
        validateNotEmpty(possibleStructures);
        return possibleStructures;
    }

    private Set<Structure> parse(final ParseContext parseContext) {
        Set<StructureTranscription> currTranscriptions = new HashSet<>();
        currTranscriptions.add(StructureTranscription.newInstance(startState, Structure.newBuilder()));
        while (parseContext.hasNext()) {
            currTranscriptions = step(parseContext.getParsePolicy(), parseContext.parseNext(), currTranscriptions);
            if (currTranscriptions.isEmpty()) { // There's no matching state => this node has no matching structure.
                throw new IllegalStateException("Node structure is invalid: not matching the given tokens.");
            }
        }

        // One final step to consume epsilon transitions.
        currTranscriptions = step(parseContext.getParsePolicy(), null, currTranscriptions);

        final Set<Structure> availableStructures = new HashSet<>();
        for (final StructureTranscription transcription : currTranscriptions) {
            if (transcription.hasFinished()) {
                availableStructures.add(transcription.get());
            }
        }

        return availableStructures;
    }

    private Set<StructureTranscription> step(final ParsePolicy policy,
                                             final ParsedValue value,
                                             final Set<StructureTranscription> transcriptions) {
        final Set<StructureTranscription> nextTranscriptions = new HashSet<>();
        for (final StructureTranscription transcription : transcriptions) {
            nextTranscriptions.addAll(transcription.step(policy, value));
        }
        return nextTranscriptions;
    }

    /**
     * Compile the given structure into the NFA, building the states chain.
     * Bare in mind that the expression comes in prefix/polish notation (NOT postfix/reverse polish notation),
     * which differs from the Thompson original algorithm.
     */
    private static State compileNFA(final Symbol symbol) {
        /*
         * This is the nfa itself (i.e., it's represented by the start state).
         *
         * As we start from one single generic symbol, from our point of view all states are mandatory.
         * Internal logic takes responsibility of using & updating this value accordingly.
         */
        return symbol.toNFAFragment(true).close();
    }
}
