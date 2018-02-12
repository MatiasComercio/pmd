/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriteevents;

import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.newInsertRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.newRemoveRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.newReplaceRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.INSERT;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REMOVE;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REPLACE;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * <p>Record {@link net.sourceforge.pmd.autofix.RewritableNode}'s modifications as {@link RewriteEvent}s.</p>
 * <p>
 * All {@link RewriteEvent}s to be recorded SHOULD be linked to the {@code RuleViolation}
 * they are fixing in order to determine what had caused the modification to happen.
 * </p>
 * <p>This class is not able to record rewrite events for different nodes, so each node may hold its own instance.</p> // xnow: HAVE TO FIX THIS
 * <p>
 * Rewrite events occurring on the same index are merged straight away, so at all moment only one rewrite event
 * is hold for a given index. This helps to understand exactly what kind of change has the node suffer,
 * independently of how many times it has been modified.
 * </p>
 */
public class RewriteEventsRecorder {
    /**
     * All rewrite events hold by this instance. The rewrite event for a given index corresponds to the modification
     * that the child node at that position suffered.
     */
    private RewriteEvent[] rewriteEvents;

    /**
     * Record an insert operation over the given {@code parent} node.
     *
     * @param parent   The node on which a new child is being inserted.
     * @param newChild The child node being inserted.
     * @param index   The index where the new child node is being inserted.
     * @param originatingRuleViolation The rule violation originating the rewrite event to be recorded. // xnow: add to the rewrite event creation
     */
    public void recordInsert(final Node parent, final Node newChild, final int index,
                             final RuleViolation originatingRuleViolation) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(newChild);
        validateNonNullIndex(index);

        recordRewriteEvent(RewriteEventFactory.newInsertRewriteEvent(parent, newChild, index));
    }

    /**
     * Record a replace operation over the given {@code parent} node.
     *
     * @param node   The node whose child is being replaced.
     * @param oldChild The child node being replaced.
     * @param newChild The new child node that will replace the {@code oldChild} node.
     * @param index   The index of the child node being replaced.
     * @param originatingRuleViolation The rule violation originating the rewrite event to be recorded. // xnow: add to the rewrite event creation
     */
    public void recordReplace(final Node node, final Node oldChild,
                              final Node newChild, final int index,
                              final RuleViolation originatingRuleViolation) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(oldChild);
        Objects.requireNonNull(newChild);
        validateNonNullIndex(index);

        recordRewriteEvent(RewriteEventFactory.newReplaceRewriteEvent(node, oldChild, newChild, index));
    }

    /**
     * Record a remove operation over the given {@code parent} node.
     *
     * @param parent   The node whose child is being removed.
     * @param oldChild The child node being removed.
     * @param index   The index of the child node being removed.
     * @param originatingRuleViolation The rule violation originating the rewrite event to be recorded. // xnow: add to the rewrite event creation
     */
    public void recordRemove(final Node parent, final Node oldChild, final int index,
                             final RuleViolation originatingRuleViolation) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(oldChild);
        validateNonNullIndex(index);

        recordRewriteEvent(RewriteEventFactory.newRemoveRewriteEvent(parent, oldChild, index));
    }

    /**
     * @return {@code true} if this instance holds any rewrite event; {@code false} otherwise.
     */
    // xnow: do we need this method as it is or should we add a `Node parent` parameter
    public boolean hasRewriteEvents() {
        return rewriteEvents != null && rewriteEvents.length > 0;
    }

    /**
     * @return A copy of all the {@link RewriteEvent}s held by this instance (may be null).
     */
    // xnow: do we need this method as it is or should we add a `Node parent` parameter
    public RewriteEvent[] getRewriteEvents() {
        // Completely immutable as RewriteEvent has all its fields final
        return Arrays.copyOf(rewriteEvents, rewriteEvents.length);
    }

    private void validateNonNullIndex(final int index) {
        if (index < 0) {
            throw new IllegalArgumentException(String.format("index <%d> is lower than 0", index));
        }
    }

    private void recordRewriteEvent(final RewriteEvent rewriteEvent) {
        final int childIndex = rewriteEvent.getChildNodeIndex();
        if (rewriteEvents == null) {
            rewriteEvents = new RewriteEvent[childIndex + 1];
        } else if (childIndex >= rewriteEvents.length) {
            final RewriteEvent[] newRewriteEvents = new RewriteEvent[childIndex + 1];
            System.arraycopy(rewriteEvents, 0, newRewriteEvents, 0, rewriteEvents.length);
            rewriteEvents = newRewriteEvents;
        }

        final RewriteEvent oldRewriteEvent = rewriteEvents[childIndex];
        if (oldRewriteEvent == null) { // This is the first event for this child index
            rewriteEvents[childIndex] = rewriteEvent;
        } else {
            // There is a previous event for the given index => we have to merge the old node event
            //  with the new one before recording the given event
            rewriteEvents = recordMergedRewriteEvents(rewriteEvents, childIndex, oldRewriteEvent, rewriteEvent);
        }
    }

    private RewriteEvent[] recordMergedRewriteEvents(final RewriteEvent[] pRewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
        final RewriteEventType oldRewriteEventType = oldRewriteEvent.getRewriteEventType();
        final RewriteEventType newRewriteEventType = newRewriteEvent.getRewriteEventType();
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(oldRewriteEventType, newRewriteEventType);
        return rewriteEventsMerger.recordMerge(pRewriteEvents, childIndex, oldRewriteEvent, newRewriteEvent);
    }

    /**
     * Interface that describe the main method to record a merge of two rewrite events.
     */
    private interface RewriteEventsMerger {
        /**
         * <p>
         * Record a rewrite event at the given {@code rewriteEventIndex} on the given {@code rewriteEvents} array.
         * </p>
         * <p>
         * This rewrite event is the result of merging the {@code oldRewriteEvent} with the {@code newRewriteEvent}.
         * The merging policy may vary depending on the type of rewrite event that each of them (old an new)
         * represent.
         * </p>
         * <p>
         * Interface's implementations are in charge of carrying out the correct merge policy in each case.
         * </p>
         * <p>
         * <strong>The original {@code rewriteEvents} array is not modified</strong>; instead, a new updated copy
         * of the given array is returned.
         * </p>
         *
         * @param rewriteEvents     The rewrite events where to record the merged rewrite event.
         * @param rewriteEventIndex The index where to record the merged rewrite event
         * @param oldRewriteEvent   The old rewrite event to be merged with the new one.
         * @param newRewriteEvent   The new rewrite event to be merged with the old one.
         * @return An updated copy of the given {@code rewriteEvents}.
         */
        RewriteEvent[] recordMerge(RewriteEvent[] rewriteEvents, int rewriteEventIndex, RewriteEvent oldRewriteEvent, RewriteEvent newRewriteEvent);
    }

    /**
     * <p>
     * Class implementing all the different merging policies based on the type of {@code oldRewriteEvent}
     * and {@code newRewriteEvent}.
     * </p>
     * <p>
     * Each merging policy should be obtained with the method {@code getRewriteEventsMerger},
     * and not directly accessed.
     * </p>
     */
    private abstract static class RewriteEventsMergers {
        private static final RewriteEventsMerger INSERT_NEW_REWRITE_EVENT_MERGER = new RewriteEventsMerger() {
            @Override
            public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int rewriteEventIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
                validate(rewriteEventIndex, oldRewriteEvent, newRewriteEvent);
                return ArrayUtils.insert(rewriteEventIndex, rewriteEvents, newRewriteEvent);
            }
        };

        private static final RewriteEventsMerger REMOVE_ORIGINAL_REWRITE_EVENT_MERGER = new RewriteEventsMerger() {
            @Override
            public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int rewriteEventIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
                validate(rewriteEventIndex, oldRewriteEvent, newRewriteEvent);
                return ArrayUtils.remove(rewriteEvents, rewriteEventIndex);
            }
        };

        private static final RewriteEventsMerger INSERT_REWRITE_EVENTS_MERGER = new RewriteEventsMerger() {
            @Override
            public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int rewriteEventIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
                validate(rewriteEventIndex, oldRewriteEvent, newRewriteEvent);
                final RewriteEvent mergedRewriteEvent = newInsertRewriteEvent(newRewriteEvent.getParentNode(), newRewriteEvent.getNewChildNode(), rewriteEventIndex);
                rewriteEvents[rewriteEventIndex] = mergedRewriteEvent;
                return rewriteEvents;
            }
        };

        private static final RewriteEventsMerger REPLACE_REWRITE_EVENTS_MERGER = new RewriteEventsMerger() {
            @Override
            public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int rewriteEventIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
                validate(rewriteEventIndex, oldRewriteEvent, newRewriteEvent);
                final RewriteEvent mergedRewriteEvent = newReplaceRewriteEvent(newRewriteEvent.getParentNode(), oldRewriteEvent.getOldChildNode(), newRewriteEvent.getNewChildNode(), rewriteEventIndex);
                rewriteEvents[rewriteEventIndex] = mergedRewriteEvent;
                return rewriteEvents;
            }
        };

        private static final RewriteEventsMerger REMOVE_REWRITE_EVENTS_MERGER = new RewriteEventsMerger() {
            @Override
            public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int rewriteEventIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
                validate(rewriteEventIndex, oldRewriteEvent, newRewriteEvent);
                final RewriteEvent mergedRewriteEvent = newRemoveRewriteEvent(newRewriteEvent.getParentNode(), oldRewriteEvent.getOldChildNode(), rewriteEventIndex);
                rewriteEvents[rewriteEventIndex] = mergedRewriteEvent;
                return rewriteEvents;
            }
        };

        private static final RewriteEventsMerger INVALID_MERGER = new RewriteEventsMerger() {
            @Override
            public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int rewriteEventIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
                validate(rewriteEventIndex, oldRewriteEvent, newRewriteEvent);
                final String msg = String.format("Cannot merge events: <%s> -> <%s>", oldRewriteEvent.getRewriteEventType(), newRewriteEvent.getRewriteEventType());
                throw new IllegalStateException(msg);
            }
        };

        private static final RewriteEventsMerger[][] REWRITE_EVENTS_MERGERS;

        static {
            final int size = RewriteEventType.values().length;
            REWRITE_EVENTS_MERGERS = new RewriteEventsMerger[size][size];
            final int iInsert = INSERT.getIndex();
            final int iRemove = REMOVE.getIndex();
            final int iReplace = REPLACE.getIndex();

            // Insert -> Insert = both Inserts are kept
            REWRITE_EVENTS_MERGERS[iInsert][iInsert] = INSERT_NEW_REWRITE_EVENT_MERGER;

            // Insert -> Replace = Insert, with the newRewriteEvent of the Replace event
            REWRITE_EVENTS_MERGERS[iInsert][iReplace] = INSERT_REWRITE_EVENTS_MERGER;

            // Insert -> Remove = remove the original Insert event
            REWRITE_EVENTS_MERGERS[iInsert][iRemove] = REMOVE_ORIGINAL_REWRITE_EVENT_MERGER;

            // Replace -> Insert = Replace & Insert are kept
            REWRITE_EVENTS_MERGERS[iReplace][iInsert] = INSERT_NEW_REWRITE_EVENT_MERGER;

            // Replace -> Replace = Replace, with the oldRewriteEvent of the original Replace and the newRewriteEvent of the new Replace
            REWRITE_EVENTS_MERGERS[iReplace][iReplace] = REPLACE_REWRITE_EVENTS_MERGER;

            // Replace -> Remove = Remove, with the oldRewriteEvent of the original Replace
            REWRITE_EVENTS_MERGERS[iReplace][iRemove] = REMOVE_REWRITE_EVENTS_MERGER;

            // Remove -> Insert = Replace, with the oldRewriteEvent of the Remove and the newRewriteEvent of the Insert
            REWRITE_EVENTS_MERGERS[iRemove][iInsert] = REPLACE_REWRITE_EVENTS_MERGER;

            // Cannot replace or remove an already removed node
            REWRITE_EVENTS_MERGERS[iRemove][iReplace] = INVALID_MERGER;
            REWRITE_EVENTS_MERGERS[iRemove][iRemove] = INVALID_MERGER;
        }

        /**
         * @param oldEventType The old event type.
         * @param newEventType The new event type.
         * @return The rewrite events merger that implements the correct merging policy for the given
         * {@code oldEventType} and {@code newEventType}.
         */
        private static RewriteEventsMerger getRewriteEventsMerger(final RewriteEventType oldEventType, final RewriteEventType newEventType) {
            return REWRITE_EVENTS_MERGERS[oldEventType.getIndex()][newEventType.getIndex()];
        }

        private static void validate(final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
            final int oldEventIndex = oldRewriteEvent.getChildNodeIndex();
            final int newEventIndex = newRewriteEvent.getChildNodeIndex();
            if (childIndex != oldEventIndex || childIndex != newEventIndex) {
                final String msg = String.format("Invalid childIndex. childIndex: <%d>, "
                        + "oldRewriteEvent.childIndex: <%d>, newRewriteEvent.childIndex: <%d>",
                    childIndex, oldEventIndex, newEventIndex);
                throw new IllegalArgumentException(msg);
            }

            final Node oldEventParentNode = oldRewriteEvent.getParentNode();
            final Node newEventParentNode = newRewriteEvent.getParentNode();
            if (!oldEventParentNode.equals(newEventParentNode)) {
                throw new IllegalArgumentException("Parent nodes of both rewrite events should be the same.");
            }

            final Node oldEventNewChild = oldRewriteEvent.getNewChildNode();
            final Node newEventOldChild = newRewriteEvent.getOldChildNode();
            if (newEventOldChild != null && !newEventOldChild.equals(oldEventNewChild)) {
                throw new IllegalArgumentException("oldChildNode of the new record event should be "
                    + "the same as the newChildNode of the old record event in order to "
                    + "be able to merge these events");
            }
        }
    }

}
