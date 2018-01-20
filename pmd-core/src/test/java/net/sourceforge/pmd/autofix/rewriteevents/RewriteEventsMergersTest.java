package net.sourceforge.pmd.autofix.rewriteevents;

import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Before;
import org.junit.Test;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createInsertRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createRemoveRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createReplaceRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.INSERT;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REMOVE;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REPLACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RewriteEventsMergersTest {
    private static final Node PARENT_NODE = DummyNode.newInstance();
    private static final Node PARENT_NODE_2 = DummyNode.newInstance(); // TODO: use to make sure we are validating parent node correspondence among events
    private static final Node OLD_CHILD_NODE = DummyNode.newInstance();
    private static final Node NEW_CHILD_NODE = DummyNode.newInstance();
    private static final Node OLD_CHILD_NODE_2 = DummyNode.newInstance(); // TODO: use to make sure we are validating child node correspondence among events
    private static final Node NEW_CHILD_NODE_2 = DummyNode.newInstance();
    private static final int INSERT_I = 0;
    private static final int REPLACE_I = 1;
    private static final int REMOVE_I = 2;

    private static final RewriteEvent INSERT_REWRITE_EVENT = createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE);
    private static final RewriteEvent REPLACE_REWRITE_EVENT = createReplaceRewriteEvent(PARENT_NODE, REPLACE_I, OLD_CHILD_NODE, NEW_CHILD_NODE);
    private static final RewriteEvent REMOVE_REWRITE_EVENT = createRemoveRewriteEvent(PARENT_NODE, REMOVE_I, OLD_CHILD_NODE);

    private RewriteEvent[] rewriteEvents;

    @Before
    public void initializeRewriteEvents() {
        rewriteEvents = new RewriteEvent[3]; // 3 = insert + replace + remove
        rewriteEvents[INSERT_REWRITE_EVENT.getChildNodeIndex()] = INSERT_REWRITE_EVENT;
        rewriteEvents[REPLACE_REWRITE_EVENT.getChildNodeIndex()] = REPLACE_REWRITE_EVENT;
        rewriteEvents[REMOVE_REWRITE_EVENT.getChildNodeIndex()] = REMOVE_REWRITE_EVENT;
    }

    // ----------------- `Insert` As Original Event Test Cases ----------------- //

    @Test
    public void insertInsertMergerTest() {
        final int childIndex = INSERT_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(INSERT, INSERT);
        final RewriteEvent newRewriteEvent = createInsertRewriteEvent(PARENT_NODE, childIndex, NEW_CHILD_NODE_2);

        // Do the actual merge
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, INSERT_REWRITE_EVENT, newRewriteEvent);

        // Expect: the new insert event has been inserted in the given index and the other events shifted to the right

        // Check updated array size
        assertEquals(rewriteEvents.length + 1, updatedRewriteEvents.length);

        // Check updated array content
        int rewriteEventsIndex = 0;
        int updatedRewriteEventsIndex = 0;
        while (rewriteEventsIndex < rewriteEvents.length && updatedRewriteEventsIndex < updatedRewriteEvents.length) {
            if (rewriteEventsIndex == childIndex && updatedRewriteEventsIndex == childIndex) {
                assertEquals(newRewriteEvent, updatedRewriteEvents[updatedRewriteEventsIndex++]);
            } else {
                assertEquals(rewriteEvents[rewriteEventsIndex++], updatedRewriteEvents[updatedRewriteEventsIndex++]);
            }
        }
    }

    @Test
    public void insertReplaceMergerTest() {
        final int childIndex = INSERT_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(INSERT, REPLACE);
        final RewriteEvent newRewriteEvent = createReplaceRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE, NEW_CHILD_NODE_2);
        final RewriteEvent expectedMergedRewriteEvent = createInsertRewriteEvent(PARENT_NODE, childIndex, NEW_CHILD_NODE_2);

        // Do the actual merge
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, INSERT_REWRITE_EVENT, newRewriteEvent);

        // Expect: replace the original insert event with a new insert event, with the newChildNode being the newChildNode of the new replace event

        // Check updated array size
        assertEquals(rewriteEvents.length, updatedRewriteEvents.length);

        // Check updated array content
        for (int i = 0; i < updatedRewriteEvents.length; i++) {
            final RewriteEvent expectedRewriteEvent = i == childIndex ? expectedMergedRewriteEvent : rewriteEvents[i];
            assertEquals(expectedRewriteEvent, updatedRewriteEvents[i]);
        }
    }

    @Test
    public void insertRemoveMergerTest() {
        final int childIndex = INSERT_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(INSERT, REMOVE);
        final RewriteEvent newRewriteEvent = createRemoveRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE);

        // Do the actual merge
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, INSERT_REWRITE_EVENT, newRewriteEvent);

        // Expect: remove the original insert event

        // Check updated array size
        assertEquals(rewriteEvents.length - 1, updatedRewriteEvents.length);

        // Check updated array content
        int rewriteEventsIndex = 0;
        int updatedRewriteEventsIndex = 0;
        while (rewriteEventsIndex < rewriteEvents.length && updatedRewriteEventsIndex < updatedRewriteEvents.length) {
            if (rewriteEventsIndex == childIndex && updatedRewriteEventsIndex == childIndex) {
                rewriteEventsIndex++;
            } else {
                assertEquals(rewriteEvents[rewriteEventsIndex++], updatedRewriteEvents[updatedRewriteEventsIndex++]);
            }
        }
    }

    // ----------------- `Replace` As Original Event Test Cases ----------------- //
    @Test
    public void replaceInsertMergerTest() {
        final int childIndex = REPLACE_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(REPLACE, INSERT);
        final RewriteEvent newRewriteEvent = createInsertRewriteEvent(PARENT_NODE, childIndex, NEW_CHILD_NODE_2);

        // Do the actual merge
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, REPLACE_REWRITE_EVENT, newRewriteEvent);

        // Expect: the new insert event has been inserted in the given index and the other events shifted to the right

        // Check updated array size
        assertEquals(rewriteEvents.length + 1, updatedRewriteEvents.length);

        // Check updated array content
        int rewriteEventsIndex = 0;
        int updatedRewriteEventsIndex = 0;
        while (rewriteEventsIndex < rewriteEvents.length && updatedRewriteEventsIndex < updatedRewriteEvents.length) {
            if (rewriteEventsIndex == childIndex && updatedRewriteEventsIndex == childIndex) {
                assertEquals(newRewriteEvent, updatedRewriteEvents[updatedRewriteEventsIndex++]);
            } else {
                assertEquals(rewriteEvents[rewriteEventsIndex++], updatedRewriteEvents[updatedRewriteEventsIndex++]);
            }
        }
    }

    @Test
    public void replaceReplaceMergerTest() {
        final int childIndex = REPLACE_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(REPLACE, REPLACE);
        final RewriteEvent newRewriteEvent = createReplaceRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE, NEW_CHILD_NODE_2);
        final RewriteEvent expectedMergedRewriteEvent = createReplaceRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE, NEW_CHILD_NODE_2);

        // Do the actual merge
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, REPLACE_REWRITE_EVENT, newRewriteEvent);

        // Expect: replace the original replace event with a new replace event,
        //  with the oldChildNode being the oldChildNode of the original replace event,
        //  and with the newChildNode being the newChildNode of the new replace event

        // Check updated array size
        assertEquals(rewriteEvents.length, updatedRewriteEvents.length);

        // Check updated array content
        for (int i = 0; i < updatedRewriteEvents.length; i++) {
            final RewriteEvent expectedRewriteEvent = i == childIndex ? expectedMergedRewriteEvent : rewriteEvents[i];
            assertEquals(expectedRewriteEvent, updatedRewriteEvents[i]);
        }
    }

    @Test
    public void replaceRemoveMergerTest() {
        final int childIndex = REPLACE_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(REPLACE, REMOVE);
        final RewriteEvent newRewriteEvent = createRemoveRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE);
        final RewriteEvent expectedMergedRewriteEvent = createRemoveRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE);

        // Do the actual merge
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, REPLACE_REWRITE_EVENT, newRewriteEvent);

        // Expect: replace the original replace event with a new remove event,
        //  with the oldChildNode being the oldChildNode of the original replace event

        // Check updated array size
        assertEquals(rewriteEvents.length, updatedRewriteEvents.length);

        // Check updated array content
        for (int i = 0; i < updatedRewriteEvents.length; i++) {
            final RewriteEvent expectedRewriteEvent = i == childIndex ? expectedMergedRewriteEvent : rewriteEvents[i];
            assertEquals(expectedRewriteEvent, updatedRewriteEvents[i]);
        }
    }

    // ----------------- `Remove` As Original Event Test Cases ----------------- //
    @Test
    public void removeInsertMergerTest() {
        final int childIndex = REMOVE_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(REMOVE, INSERT);
        final RewriteEvent newRewriteEvent = createInsertRewriteEvent(PARENT_NODE, childIndex, NEW_CHILD_NODE_2);
        final RewriteEvent expectedMergedRewriteEvent = createReplaceRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE, NEW_CHILD_NODE_2);

        // Do the actual merge
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, REMOVE_REWRITE_EVENT, newRewriteEvent);

        // Expect: replace the original remove event with a replace event,
        //  with the oldChildNode being the oldChildNode of the original remove event,
        //  and with the newChildNode being the newChildNode of the new insert event

        // Check updated array size
        assertEquals(rewriteEvents.length, updatedRewriteEvents.length);

        // Check updated array content
        for (int i = 0; i < updatedRewriteEvents.length; i++) {
            final RewriteEvent expectedRewriteEvent = i == childIndex ? expectedMergedRewriteEvent : rewriteEvents[i];
            assertEquals(expectedRewriteEvent, updatedRewriteEvents[i]);
        }
    }

    @Test
    public void removeReplaceMergerTest() {
        final int childIndex = REMOVE_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(REMOVE, REPLACE);
        final RewriteEvent newRewriteEvent = createReplaceRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE, NEW_CHILD_NODE_2);
        try {
            // Do the actual merge
            rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, REMOVE_REWRITE_EVENT, newRewriteEvent);
            // Expecting fail as a remove event cannot be followed by a replace event.
            //  This would mean that an already removed node is then trying to be replaced, which makes no sense
            fail();
        } catch (final Exception ignored) {
            // Expected flow
        }
    }

    @Test
    public void removeRemoveMergerTest() {
        final int childIndex = REMOVE_I;
        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(REMOVE, REPLACE);
        final RewriteEvent newRewriteEvent = createRemoveRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE);
        try {
            // Do the actual merge
            rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, REMOVE_REWRITE_EVENT, newRewriteEvent);
            // Expecting fail as a remove event cannot be followed by a replace event.
            //  This would mean that an already removed node is then trying to be replaced, which makes no sense
            fail();
        } catch (final Exception ignored) {
            // Expected flow
        }
    }

}
