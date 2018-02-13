/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.Node;

// xaf: update documentation
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
    private final Map<Node, RewriteRecords> rewriteRecordsPerParentNode;

    public RewriteEventsRecorder() {
        this.rewriteRecordsPerParentNode = new HashMap<>();
    }

    public void record(final Node parentNode, final RewriteEvent rewriteEvent) {
        RewriteRecords rewriteRecords = rewriteRecordsPerParentNode.get(parentNode);
        if (rewriteRecords == null) {
            rewriteRecords = new RewriteRecords();
            rewriteRecordsPerParentNode.put(parentNode, rewriteRecords);
        }
        rewriteRecords.record(rewriteEvent);
    }

    private static class RewriteRecords {
        private final Map<Node, RewriteEvent> mergeableRewriteEvents;
        private final Set<RewriteEvent> nonMergeableRewriteEvents;

        private RewriteRecords() {
            mergeableRewriteEvents = new HashMap<>();
            nonMergeableRewriteEvents = new HashSet<>();
        }

        public void record(final RewriteEvent rewriteEvent) {
            // Merge this rewrite event with a compatible & existing one, if possible.
            // `compatible` means that oldRewriteEvent.newChildNode == newRewriteEvent.oldChildNode,
            //  as defined by the `RewriteEvent#mergeWith(RewriteEvent)` method documentation.
            final RewriteEvent newRewriteEvent;
            final Node searchMergeNode = rewriteEvent.getOldChildNode();
            if (searchMergeNode == null) {
                newRewriteEvent = rewriteEvent;
            } else {
                final RewriteEvent oldRewriteEvent = mergeableRewriteEvents.remove(searchMergeNode);
                newRewriteEvent = oldRewriteEvent == null ? rewriteEvent : oldRewriteEvent.mergeWith(rewriteEvent);
                // Recall that if the merge is performed, then a null return value means that
                //  oldRewriteEvent and rewriteEvent cancelled themselves.
            }

            if (newRewriteEvent == null) {
                return; // The record should not be performed
            }

            // A rewrite event is mergeable if it may be linked/merged with a newer rewrite event
            final Node keyMergeNode = newRewriteEvent.getNewChildNode();
            if (keyMergeNode == null) {
                nonMergeableRewriteEvents.add(rewriteEvent);
            } else {
                mergeableRewriteEvents.put(keyMergeNode, rewriteEvent);
            }
        }
    }
}
