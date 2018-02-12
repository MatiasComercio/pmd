/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import static net.sourceforge.pmd.autofix.rewrite.RewriteEventType.INSERT;
import static net.sourceforge.pmd.autofix.rewrite.RewriteEventType.REMOVE;
import static net.sourceforge.pmd.autofix.rewrite.RewriteEventType.REPLACE;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

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
public class RewriteEventsRecorder { // xaf: perhaps updating this to `RewriteRecorder`
    /**
     * All rewrite events hold by this instance, cataloged by parent node.
     * The rewrite event for a given index corresponds to the modification the child node at that position suffered.
     */
    private Map<Node, RewriteEvent[]> rewriteEventsPerNode;

    public RewriteEventsRecorder() {
        this.rewriteEventsPerNode = new HashMap<>();
    }

    // xnow: document
    public void record(final Node parentNode, final int rewriteIndex, final RewriteEvent rewriteEvent) {
        RewriteEvent[] rewriteEvents = rewriteEventsPerNode.get(parentNode);
        if (rewriteEvents == null) {
            rewriteEvents = new RewriteEvent[rewriteIndex + 1];
            rewriteEventsPerNode.put(parentNode, rewriteEvents);
        } else if (rewriteIndex >= rewriteEvents.length) {
            final RewriteEvent[] newRewriteEvents = new RewriteEvent[rewriteIndex + 1];
            System.arraycopy(rewriteEvents, 0, newRewriteEvents, 0, rewriteEvents.length);
            rewriteEvents = newRewriteEvents;
            rewriteEventsPerNode.put(parentNode, rewriteEvents);
        }

        final RewriteEvent oldRewriteEvent = rewriteEvents[rewriteIndex];
        if (oldRewriteEvent == null) { // This is the first event for this child index
            rewriteEvents[rewriteIndex] = rewriteEvent;
        } else {
            // There is a previous event for the given index => we have to merge the old node event
            //  with the new one before recording the given event
            rewriteEvents = recordMergedRewriteEvents(rewriteEvents, rewriteIndex, oldRewriteEvent, rewriteEvent);
        }
    }

    private RewriteEvent[] recordMergedRewriteEvents(final RewriteEvent[] pRewriteEvents, final int childIndex,
                                                     final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(oldRewriteEvent, newRewriteEvent);
        return rewriteEventsMerger.recordMerge(pRewriteEvents, childIndex, oldRewriteEvent, newRewriteEvent);
    }

    /**
     * Interface that describe the main method to record a merge of two rewrite events.
     */
    private interface RewriteEventsMerger {
        /**
         * <p>
         * Record a rewrite event at the given {@code rewriteEventIndex} on the given {@code rewriteEventsPerNode} array.
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
         * <strong>The original {@code rewriteEventsPerNode} array is not modified</strong>; instead, a new updated copy
         * of the given array is returned.
         * </p>
         *
         * @param rewriteEvents     The rewrite events where to record the merged rewrite event.
         * @param rewriteEventIndex The index where to record the merged rewrite event
         * @param oldRewriteEvent   The old rewrite event to be merged with the new one.
         * @param newRewriteEvent   The new rewrite event to be merged with the old one.
         * @return An updated copy of the given {@code rewriteEventsPerNode}.
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

        private static final int INSERT_I = 0;
        private static final int REPLACE_I = 1;
        private static final int REMOVE_I = 2;
        private static final Map<Class<? extends RewriteEvent>, Integer> EVENT_TO_INDEX_MAPPER;
        private static final RewriteEventsMerger[][] REWRITE_EVENTS_MERGERS;

        static {
            EVENT_TO_INDEX_MAPPER = new HashMap<>();
            EVENT_TO_INDEX_MAPPER.put(InsertEvent.class, INSERT_I);
            EVENT_TO_INDEX_MAPPER.put(ReplaceEvent.class, REPLACE_I);
            EVENT_TO_INDEX_MAPPER.put(RemoveEvent.class, REMOVE_I);


            final int size = RewriteEventType.values().length;
            REWRITE_EVENTS_MERGERS = new RewriteEventsMerger[size][size];
            final int iInsert = INSERT_I;
            final int iReplace = REPLACE_I;
            final int iRemove = REMOVE_I;

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
        private static RewriteEventsMerger getRewriteEventsMerger(final RewriteEvent oldEvent, final RewriteEvent newEvent) {
            return REWRITE_EVENTS_MERGERS[mapToIndex(oldEvent)][mapToIndex(newEvent)];
        }

        private static int mapToIndex(final RewriteEvent event) {
            return EVENT_TO_INDEX_MAPPER.get(event.getClass());
        }

        private static void validate(final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
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
