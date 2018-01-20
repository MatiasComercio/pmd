/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewriteevents;

import java.util.Arrays;
import java.util.Objects;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * xnow document
 */
public class RewriteEventsRecorderImpl implements RewriteEventsRecorder {
    // xnow: document
    // List of node events is ordered based on the child index for the parent node, so, only one node event is recorded for each child of a parent node
    private RewriteEvent[] rewriteEvents;

    @Override
    public void recordRemove(final Node parentNode, final Node oldChildNode, final int childIndex) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(oldChildNode);
        validateNonNullIndex(childIndex);

        recordRewriteEvent(RewriteEventFactory.createRemoveRewriteEvent(parentNode, childIndex, oldChildNode));
    }

    @Override
    public void recordInsert(final Node parentNode, final Node newChildNode, final int childIndex) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(newChildNode);
        validateNonNullIndex(childIndex);

        recordRewriteEvent(RewriteEventFactory.createInsertRewriteEvent(parentNode, childIndex, newChildNode));
    }

    @Override
    public void recordReplace(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(oldChildNode);
        Objects.requireNonNull(newChildNode);
        validateNonNullIndex(childIndex);

        recordRewriteEvent(RewriteEventFactory.createReplaceRewriteEvent(parentNode, childIndex, oldChildNode, newChildNode));
    }

    @Override
    public boolean hasRewriteEvents() {
        return rewriteEvents != null && rewriteEvents.length > 0;
    }

    @Override
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

    private RewriteEvent[] recordMergedRewriteEvents(final RewriteEvent[] rewriteEvents, final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
        final RewriteEventType oldRewriteEventType = oldRewriteEvent.getRewriteEventType();
        final RewriteEventType newRewriteEventType = newRewriteEvent.getRewriteEventType();
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(oldRewriteEventType, newRewriteEventType);
        return rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, oldRewriteEvent, newRewriteEvent);
    }
}
