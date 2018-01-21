package net.sourceforge.pmd.autofix.rewriteevents;

import junitparams.Parameters;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RewriteEventsRecorderImplTest {
//
//    // xnow document
//    void recordRemove(Node parentNode, Node oldChildNode, int childIndex);
//
//    // xnow document
//    void recordInsert(Node parentNode, Node newChildNode, int childIndex);
//
//    // xnow document
//    void recordReplace(Node parentNode, Node oldChildNode, Node newChildNode, int childIndex);
//
//    // xnow document
//    boolean hasRewriteEvents();
//
//    // xnow document
//    RewriteEvent[] getRewriteEvents();

    private static final Node PARENT_NODE = DummyNode.newInstance();
    private static final Node PARENT_NODE_2 = DummyNode.newInstance();
    private static final Node OLD_CHILD_NODE = DummyNode.newInstance();
    private static final Node NEW_CHILD_NODE = DummyNode.newInstance();
    private static final Node OLD_CHILD_NODE_2 = DummyNode.newInstance();
    private static final Node NEW_CHILD_NODE_2 = DummyNode.newInstance();
    private static final int INSERT_I = 0;
    private static final int REPLACE_I = 1;
    private static final int REMOVE_I = 2;

    private static final RewriteEvent INSERT_REWRITE_EVENT = createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE);
    private static final RewriteEvent REPLACE_REWRITE_EVENT = createReplaceRewriteEvent(PARENT_NODE, REPLACE_I, OLD_CHILD_NODE, NEW_CHILD_NODE);
    private static final RewriteEvent REMOVE_REWRITE_EVENT = createRemoveRewriteEvent(PARENT_NODE, REMOVE_I, OLD_CHILD_NODE);

    private RewriteEventsRecorder rewriteEventsRecorder;

    @Before
    public void initializeRewriteEventsRecorder() {
        rewriteEventsRecorder = new RewriteEventsRecorderImpl();
    }

    // -----------------*** Single Rewrite Events Test Cases ***----------------- //

    @Test
    public void testRecordRemove() {
        // Do the actual record
        rewriteEventsRecorder.recordRemove(PARENT_NODE, OLD_CHILD_NODE, REMOVE_I);

        assertTrue(rewriteEventsRecorder.hasRewriteEvents());
        assertEquals(REMOVE_REWRITE_EVENT, rewriteEventsRecorder.getRewriteEvents()[REMOVE_I]);
    }

    // -----------------** Merge Rewrite Events Test Cases ***----------------- //
    // -----------------* Valid Merge Rewrite Events Test Cases *----------------- //
    // ----------------- `Insert` As Original Event Test Cases ----------------- //
    @Test
    public void insertInsertMergerTest() {
        final int childIndex = INSERT_I;
        // Record the original event
        rewriteEventsRecorder.recordInsert(PARENT_NODE, NEW_CHILD_NODE, childIndex);
        final RewriteEvent[] rewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Record the new event
        rewriteEventsRecorder.recordInsert(PARENT_NODE, NEW_CHILD_NODE_2, childIndex);

        final RewriteEvent expectedNewRewriteEvent = createInsertRewriteEvent(PARENT_NODE, childIndex, NEW_CHILD_NODE_2);
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Expect: the new insert event has been inserted in the given index and the other events shifted to the right

        // Check updated array size
        assertEquals(rewriteEvents.length + 1, updatedRewriteEvents.length);

        // Check updated array content
        int rewriteEventsIndex = 0;
        int updatedRewriteEventsIndex = 0;
        while (rewriteEventsIndex < rewriteEvents.length && updatedRewriteEventsIndex < updatedRewriteEvents.length) {
            if (rewriteEventsIndex == childIndex && updatedRewriteEventsIndex == childIndex) {
                assertEquals(expectedNewRewriteEvent, updatedRewriteEvents[updatedRewriteEventsIndex++]);
            } else {
                assertEquals(rewriteEvents[rewriteEventsIndex++], updatedRewriteEvents[updatedRewriteEventsIndex++]);
            }
        }
    }

    @Test
    public void insertReplaceMergerTest() {
        final int childIndex = INSERT_I;

        // Record the original event
        rewriteEventsRecorder.recordInsert(PARENT_NODE, NEW_CHILD_NODE, childIndex);
        final RewriteEvent[] rewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Record the new event
        rewriteEventsRecorder.recordReplace(PARENT_NODE, NEW_CHILD_NODE, NEW_CHILD_NODE_2, childIndex);

        final RewriteEvent expectedMergedRewriteEvent = createInsertRewriteEvent(PARENT_NODE, childIndex, NEW_CHILD_NODE_2);
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();

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

        // Record the original event
        rewriteEventsRecorder.recordInsert(PARENT_NODE, NEW_CHILD_NODE, childIndex);
        final RewriteEvent[] rewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Record the new event
        rewriteEventsRecorder.recordRemove(PARENT_NODE, NEW_CHILD_NODE, childIndex);

        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();

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

        // Record the original event
        rewriteEventsRecorder.recordReplace(PARENT_NODE, OLD_CHILD_NODE, NEW_CHILD_NODE, childIndex);
        final RewriteEvent[] rewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Record the new event
        rewriteEventsRecorder.recordInsert(PARENT_NODE, NEW_CHILD_NODE_2, childIndex);

        final RewriteEvent newRewriteEvent = createInsertRewriteEvent(PARENT_NODE, childIndex, NEW_CHILD_NODE_2);
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();

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

        // Record the original event
        rewriteEventsRecorder.recordReplace(PARENT_NODE, OLD_CHILD_NODE, NEW_CHILD_NODE, childIndex);
        final RewriteEvent[] rewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Record the new event
        rewriteEventsRecorder.recordReplace(PARENT_NODE, NEW_CHILD_NODE, NEW_CHILD_NODE_2, childIndex);

        final RewriteEvent expectedMergedRewriteEvent = createReplaceRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE, NEW_CHILD_NODE_2);
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();

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

        // Record the original event
        rewriteEventsRecorder.recordReplace(PARENT_NODE, OLD_CHILD_NODE, NEW_CHILD_NODE, childIndex);
        final RewriteEvent[] rewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Record the new event
        rewriteEventsRecorder.recordRemove(PARENT_NODE, NEW_CHILD_NODE, childIndex);

        final RewriteEvent expectedMergedRewriteEvent = createRemoveRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE);
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();

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

        // Record the original event
        rewriteEventsRecorder.recordRemove(PARENT_NODE, OLD_CHILD_NODE, childIndex);
        final RewriteEvent[] rewriteEvents = rewriteEventsRecorder.getRewriteEvents();

        // Record the new event
        rewriteEventsRecorder.recordInsert(PARENT_NODE, NEW_CHILD_NODE_2, childIndex);

        final RewriteEvent expectedMergedRewriteEvent = createReplaceRewriteEvent(PARENT_NODE, childIndex, OLD_CHILD_NODE, NEW_CHILD_NODE_2);
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();

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

    // removeReplace & removeRemove are both invalid cases; check below

//    // -----------------* Invalid Merge Rewrite Events Test Cases *----------------- //
//
//    @SuppressWarnings("unused") // Used by JUnitParams in `invalidMergeRewriteEventsTest` test case
//    private Object invalidMergeRewriteEventsTestParameters() {
//        final DummyNode node = new DummyNode(0);
//        return new Object[] {
//            // Insert cases
//            new Object[] {-1, INSERT_REWRITE_EVENT, INSERT_REWRITE_EVENT}, // Invalid index
//            new Object[] {INSERT_I, INSERT_REWRITE_EVENT, createInsertRewriteEvent(PARENT_NODE_2, INSERT_I, NEW_CHILD_NODE)}, // Not the same parent
//            new Object[] {INSERT_I, INSERT_REWRITE_EVENT, createInsertRewriteEvent(PARENT_NODE, REPLACE_I, NEW_CHILD_NODE)}, // Not the same index
//            // Replace cases
//            new Object[] {-1, REPLACE_REWRITE_EVENT, INSERT_REWRITE_EVENT}, // Invalid index
//            new Object[] {REPLACE_I, REPLACE_REWRITE_EVENT, createInsertRewriteEvent(PARENT_NODE_2, INSERT_I, NEW_CHILD_NODE)}, // Not the same parent
//            new Object[] {REPLACE_I, REPLACE_REWRITE_EVENT, createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE)}, // Not the same index
//            // [replace->replace] oldChildNode of the new event should be the same as the newChildNode of the original event for merging
//            new Object[] {REPLACE_I, REPLACE_REWRITE_EVENT, createReplaceRewriteEvent(PARENT_NODE_2, INSERT_I, OLD_CHILD_NODE_2, NEW_CHILD_NODE_2)},
//            // [replace->remove] oldChildNode of the new event should be the same as the newChildNode of the original event for merging
//            new Object[] {REPLACE_I, REPLACE_REWRITE_EVENT, createRemoveRewriteEvent(PARENT_NODE_2, INSERT_I, OLD_CHILD_NODE_2)},
//            // Remove cases
//            new Object[] {-1, REMOVE_REWRITE_EVENT, INSERT_REWRITE_EVENT}, // Invalid index
//            new Object[] {REMOVE_I, REMOVE_REWRITE_EVENT, createInsertRewriteEvent(PARENT_NODE_2, INSERT_I, NEW_CHILD_NODE)}, // Not the same parent
//            new Object[] {REMOVE_I, REMOVE_REWRITE_EVENT, createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE)}, // Not the same index
//            // Expecting fail as a remove event cannot be followed by a replace event.
//            //  This would mean that an already removed node is then trying to be replaced, which makes no sense
//            new Object[] {REMOVE_I, REMOVE_REWRITE_EVENT, createReplaceRewriteEvent(PARENT_NODE, REMOVE_I, OLD_CHILD_NODE, NEW_CHILD_NODE_2)},
//            // Expecting fail as a remove event cannot be followed by another remove event.
//            //  This would mean that an already removed node is then trying to be removed again, which makes no sense
//            new Object[] {REMOVE_I, REMOVE_REWRITE_EVENT, createRemoveRewriteEvent(PARENT_NODE, REMOVE_I, OLD_CHILD_NODE)}
//        };
//    }
//
//    @Test
//    @Parameters(method = "invalidMergeRewriteEventsTestParameters")
//    public void invalidMergeRewriteEventsTest(final int childIndex, final RewriteEvent oldRewriteEvent, final RewriteEvent newRewriteEvent) {
//        final RewriteEventsMerger rewriteEventsMerger = RewriteEventsMergers.getRewriteEventsMerger(oldRewriteEvent.getRewriteEventType(), newRewriteEvent.getRewriteEventType());
//        try {
//            // Do the actual merge
//            rewriteEventsMerger.recordMerge(rewriteEvents, childIndex, oldRewriteEvent, newRewriteEvent);
//            // Expecting fail as a remove event cannot be followed by a replace event.
//            //  This would mean that an already removed node is then trying to be replaced, which makes no sense
//            fail();
//        } catch (final Exception ignored) {
//            // Expected flow
//        }
//    }
}
