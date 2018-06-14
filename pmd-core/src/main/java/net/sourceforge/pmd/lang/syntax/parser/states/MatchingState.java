/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.states;

/**
 * Marker interface that represents a state
 * that needs a concrete value to transition from this state to its outputs states
 * when it matches the given value in the transcribe method.
 */
public interface MatchingState extends State {
}
