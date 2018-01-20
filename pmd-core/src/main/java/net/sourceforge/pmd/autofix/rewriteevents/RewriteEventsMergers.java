package net.sourceforge.pmd.autofix.rewriteevents;

import org.apache.commons.lang3.ArrayUtils;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createInsertRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createRemoveRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createReplaceRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.INSERT;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REMOVE;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REPLACE;

public abstract class RewriteEventsMergers {
    private static final RewriteEventsMerger INSERT_NEW_REWRITE_EVENT_MERGER = new RewriteEventsMerger() {
        @Override
        public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
            return ArrayUtils.insert(childIndex, rewriteEvents, newRewriteEvent);
        }
    };

    private static final RewriteEventsMerger REMOVE_ORIGINAL_REWRITE_EVENT_MERGER = new RewriteEventsMerger() {
        @Override
        public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
            return ArrayUtils.remove(rewriteEvents, childIndex);
        }
    };

    private static final RewriteEventsMerger INSERT_REWRITE_EVENTS_MERGER = new RewriteEventsMerger() {
        @Override
        public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
            final RewriteEvent mergedRewriteEvent = createInsertRewriteEvent(newRewriteEvent.getParentNode(), childIndex, newRewriteEvent.getNewChildNode());
            rewriteEvents[childIndex] = mergedRewriteEvent;
            return rewriteEvents;
        }
    };

    private static final RewriteEventsMerger REPLACE_REWRITE_EVENTS_MERGER = new RewriteEventsMerger() {
        @Override
        public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
            final RewriteEvent mergedRewriteEvent = createReplaceRewriteEvent(newRewriteEvent.getParentNode(), childIndex, oldRewriteEvent.getOldChildNode(), newRewriteEvent.getNewChildNode());
            rewriteEvents[childIndex] = mergedRewriteEvent;
            return rewriteEvents;
        }
    };

    private static final RewriteEventsMerger REMOVE_REWRITE_EVENTS_MERGER = new RewriteEventsMerger() {
        @Override
        public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
            final RewriteEvent mergedRewriteEvent = createRemoveRewriteEvent(newRewriteEvent.getParentNode(), childIndex, oldRewriteEvent.getOldChildNode());
            rewriteEvents[childIndex] = mergedRewriteEvent;
            return rewriteEvents;
        }
    };

    private static final RewriteEventsMerger INVALID_MERGER = new RewriteEventsMerger() {
        @Override
        public RewriteEvent[] recordMerge(final RewriteEvent[] rewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
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

    public static RewriteEventsMerger getRewriteEventsMerger(final RewriteEventType oldEventType, final RewriteEventType newEventType) {
        return REWRITE_EVENTS_MERGERS[oldEventType.getIndex()][newEventType.getIndex()];
    }
}
