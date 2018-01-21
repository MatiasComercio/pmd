package net.sourceforge.pmd.autofix.rewriteevents;

import junitparams.Parameters;
import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Before;
import org.junit.Test;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createInsertRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createRemoveRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createReplaceRewriteEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/*
 * TODO:
 * - Invalid index cases
 */

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

//    private static final RewriteEvent INSERT_REWRITE_EVENT = createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE);
//    private static final RewriteEvent REPLACE_REWRITE_EVENT = createReplaceRewriteEvent(PARENT_NODE, REPLACE_I, OLD_CHILD_NODE, NEW_CHILD_NODE);
//    private static final RewriteEvent REMOVE_REWRITE_EVENT = createRemoveRewriteEvent(PARENT_NODE, REMOVE_I, OLD_CHILD_NODE);

    private static final Recorder ORIGINAL_INSERT_RECORDER = new InsertRecorder(PARENT_NODE, NEW_CHILD_NODE, INSERT_I);
    private static final Recorder ORIGINAL_REPLACE_RECORDER = new ReplaceRecorder(PARENT_NODE, OLD_CHILD_NODE, NEW_CHILD_NODE, REPLACE_I);
    private static final Recorder ORIGINAL_REMOVE_RECORDER = new RemoveRecorder(PARENT_NODE, OLD_CHILD_NODE, REMOVE_I);

    private RewriteEventsRecorder rewriteEventsRecorder;

    @Before
    public void initializeRewriteEventsRecorder() {
        rewriteEventsRecorder = new RewriteEventsRecorderImpl();
    }

    // -----------------*** Single Rewrite Events Test Cases ***----------------- //

//    @Test
//    public void testRecordRemove() {
//        // Do the actual record
//        rewriteEventsRecorder.recordRemove(PARENT_NODE, OLD_CHILD_NODE, REMOVE_I);
//
//        assertTrue(rewriteEventsRecorder.hasRewriteEvents());
//        assertEquals(REMOVE_REWRITE_EVENT, rewriteEventsRecorder.getRewriteEvents()[REMOVE_I]);
//    }

    // -----------------** Merge Rewrite Events Test Cases ***----------------- //
    // -----------------* Valid Merge Rewrite Events Test Cases *----------------- //
    // ----------------- `Insert` As Original Event Test Cases ----------------- //
    @Test
    public void insertInsertMergerTest() {
        final int childIndex = INSERT_I;
        // Record the original event
        ORIGINAL_INSERT_RECORDER.record(rewriteEventsRecorder);
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
        ORIGINAL_INSERT_RECORDER.record(rewriteEventsRecorder);
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
        ORIGINAL_INSERT_RECORDER.record(rewriteEventsRecorder);
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
        ORIGINAL_REPLACE_RECORDER.record(rewriteEventsRecorder);
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
        ORIGINAL_REPLACE_RECORDER.record(rewriteEventsRecorder);
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
        ORIGINAL_REPLACE_RECORDER.record(rewriteEventsRecorder);
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
        ORIGINAL_REMOVE_RECORDER.record(rewriteEventsRecorder);
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

    // -----------------* Invalid Merge Rewrite Events Test Cases *----------------- //

    @SuppressWarnings("unused") // Used by JUnitParams in `invalidMergeRewriteEventsTest` test case
    private Object invalidMergeRewriteEventsTestParameters() {
        final DummyNode node = new DummyNode(0);
        final Recorder newParentInsertRecorder = new InsertRecorder(PARENT_NODE_2, NEW_CHILD_NODE_2, INSERT_I);
        final Recorder newParentReplaceRecorder = new ReplaceRecorder(PARENT_NODE_2, OLD_CHILD_NODE, NEW_CHILD_NODE_2, INSERT_I);
        final Recorder newParentRemoveRecorder = new InsertRecorder(PARENT_NODE_2, OLD_CHILD_NODE, INSERT_I);
        return new Object[] {
            // Insert cases
            new Object[] {ORIGINAL_INSERT_RECORDER, newParentInsertRecorder}, // Not the same parent
            new Object[] {ORIGINAL_INSERT_RECORDER, newParentReplaceRecorder}, // Not the same parent
            new Object[] {ORIGINAL_INSERT_RECORDER, newParentRemoveRecorder}, // Not the same parent
            // Replace cases
            new Object[] {ORIGINAL_REPLACE_RECORDER, newParentInsertRecorder}, // Not the same parent
            new Object[] {ORIGINAL_REPLACE_RECORDER, newParentReplaceRecorder}, // Not the same parent
            new Object[] {ORIGINAL_REPLACE_RECORDER, newParentRemoveRecorder}, // Not the same parent
            // - [replace->replace] oldChildNode of the new event should be the same as the newChildNode of the original event for merging
            new Object[] {ORIGINAL_REPLACE_RECORDER, new ReplaceRecorder(PARENT_NODE, OLD_CHILD_NODE_2, NEW_CHILD_NODE_2, REPLACE_I)},
            // - [replace->remove] oldChildNode of the new event should be the same as the newChildNode of the original event for merging
            new Object[] {ORIGINAL_REPLACE_RECORDER, new RemoveRecorder(PARENT_NODE, OLD_CHILD_NODE_2, REPLACE_I)},
            // Remove cases
            new Object[] {ORIGINAL_REMOVE_RECORDER, newParentInsertRecorder}, // Not the same parent
            new Object[] {ORIGINAL_REMOVE_RECORDER, newParentReplaceRecorder}, // Not the same parent
            new Object[] {ORIGINAL_REMOVE_RECORDER, newParentRemoveRecorder}, // Not the same parent
            // - Expecting fail as a remove event cannot be followed by a replace event.
            //      This would mean that an already removed node is then trying to be replaced, which makes no sense
            new Object[] {ORIGINAL_REMOVE_RECORDER, new ReplaceRecorder(PARENT_NODE, OLD_CHILD_NODE, NEW_CHILD_NODE_2, REMOVE_I)},
            // - Expecting fail as a remove event cannot be followed by another remove event.
            //      This would mean that an already removed node is then trying to be removed again, which makes no sense
            new Object[] {ORIGINAL_REMOVE_RECORDER, new RemoveRecorder(PARENT_NODE, OLD_CHILD_NODE, REMOVE_I)},
        };
    }

    @Test
    @Parameters(method = "invalidMergeRewriteEventsTestParameters")
    public void invalidMergeRewriteEventsTest(final Recorder originalEventRecorder, final Recorder newEventRecorder) {
        // Record the original event
        originalEventRecorder.record(rewriteEventsRecorder);

        try {
            // Record the new event
            newEventRecorder.record(rewriteEventsRecorder);
            fail(); // Reach here if the expected exception has not been thrown
        } catch (final Exception ignored) {
            // Expected flow
        }
    }

    private interface Recorder {
        void record(RewriteEventsRecorder rewriteEventsRecorder);
    }

    private static abstract class AbstractRecorder implements Recorder {
        /* package-private */ final Node parentNode;
        /* package-private */ final Node oldChildNode;
        /* package-private */ final Node newChildNode;
        /* package-private */ final int childIndex;


        /* package-private */ AbstractRecorder(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {
            this.parentNode = parentNode;
            this.oldChildNode = oldChildNode;
            this.newChildNode = newChildNode;
            this.childIndex = childIndex;
        }
    }

    private static class InsertRecorder extends AbstractRecorder {
        private InsertRecorder(final Node parentNode, final Node newChildNode, final int childIndex) {
            super(parentNode, null, newChildNode, childIndex);
        }

        @Override
        public void record(final RewriteEventsRecorder rewriteEventsRecorder) {
            rewriteEventsRecorder.recordInsert(parentNode, newChildNode, childIndex);
        }
    }

    private static class ReplaceRecorder extends AbstractRecorder {
        private ReplaceRecorder(final Node parentNode, final Node oldChildNode, final Node newChildNode, final int childIndex) {
            super(parentNode, oldChildNode, newChildNode, childIndex);
        }

        @Override
        public void record(final RewriteEventsRecorder rewriteEventsRecorder) {
            rewriteEventsRecorder.recordReplace(parentNode, oldChildNode, newChildNode, childIndex);
        }
    }

    private static class RemoveRecorder extends AbstractRecorder {
        private RemoveRecorder(final Node parentNode, final Node oldChildNode, final int childIndex) {
            super(parentNode, oldChildNode, null, childIndex);
        }

        @Override
        public void record(final RewriteEventsRecorder rewriteEventsRecorder) {
            rewriteEventsRecorder.recordRemove(parentNode, oldChildNode, childIndex);
        }
    }
}
