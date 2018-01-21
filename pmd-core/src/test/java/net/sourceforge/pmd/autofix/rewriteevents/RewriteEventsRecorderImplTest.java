package net.sourceforge.pmd.autofix.rewriteevents;

import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createInsertRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createRemoveRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createReplaceRewriteEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * TODO:
 * - Invalid index cases
 */
@RunWith(JUnitParamsRunner.class)
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
    private Object testValidMergeRewriteEventsParameters() {
        return new Object[] {
            // `Insert` As Original Event Test Cases
            new Object[] { // insert -> insert
                ORIGINAL_INSERT_RECORDER,
                new InsertRecorder(PARENT_NODE, NEW_CHILD_NODE_2, INSERT_I),
                createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE_2),
                INSERT_I,
                new InsertedNewRewriteEventExpectation()
            },
            new Object[] { // insert -> replace
                ORIGINAL_INSERT_RECORDER,
                new ReplaceRecorder(PARENT_NODE, NEW_CHILD_NODE, NEW_CHILD_NODE_2, INSERT_I),
                createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE_2),
                INSERT_I,
                new ReplacedOriginalRewriteEventExpectation()
            },
            new Object[] { // insert -> remove
                ORIGINAL_INSERT_RECORDER,
                new RemoveRecorder(PARENT_NODE, NEW_CHILD_NODE, INSERT_I),
                null, // not expecting new rewrite event
                INSERT_I,
                new RemovedOriginalRewriteEventExpectation()
            },
            // `Replace` As Original Event Test Cases
            new Object[] { // replace -> insert
                ORIGINAL_REPLACE_RECORDER,
                new InsertRecorder(PARENT_NODE, NEW_CHILD_NODE_2, REPLACE_I),
                createInsertRewriteEvent(PARENT_NODE, REPLACE_I, NEW_CHILD_NODE_2),
                REPLACE_I,
                new InsertedNewRewriteEventExpectation()
            },
            new Object[] { // replace -> replace
                ORIGINAL_REPLACE_RECORDER,
                new ReplaceRecorder(PARENT_NODE, NEW_CHILD_NODE, NEW_CHILD_NODE_2, REPLACE_I),
                createReplaceRewriteEvent(PARENT_NODE, REPLACE_I, OLD_CHILD_NODE, NEW_CHILD_NODE_2),
                REPLACE_I,
                new ReplacedOriginalRewriteEventExpectation()
            },
            new Object[] { // replace -> remove
                ORIGINAL_REPLACE_RECORDER,
                new RemoveRecorder(PARENT_NODE, NEW_CHILD_NODE, REPLACE_I),
                createRemoveRewriteEvent(PARENT_NODE, REPLACE_I, OLD_CHILD_NODE),
                REPLACE_I,
                new ReplacedOriginalRewriteEventExpectation()
            },
            // `Remove` As Original Event Test Cases
            new Object[] { // remove -> insert
                ORIGINAL_REMOVE_RECORDER,
                new InsertRecorder(PARENT_NODE, NEW_CHILD_NODE_2, REMOVE_I),
                createReplaceRewriteEvent(PARENT_NODE, REMOVE_I, OLD_CHILD_NODE, NEW_CHILD_NODE_2),
                REMOVE_I,
                new ReplacedOriginalRewriteEventExpectation()
            }
            // remove -> replace & remove -> remove are both invalid cases; check `testInvalidMergeRewriteEvents`
        };
    }

    @Test
    @Parameters(method = "testValidMergeRewriteEventsParameters")
    public void testValidMergeRewriteEvents(final Recorder originalEventRecorder,
                                            final Recorder newEventRecorder,
                                            final RewriteEvent expectedNewRewriteEvent,
                                            final int rewriteEventIndex,
                                            final Expectation expectation) {
        // Record the original event
        originalEventRecorder.record(rewriteEventsRecorder);
        // Grab the original rewrite events
        final RewriteEvent[] originalRewriteEvents = rewriteEventsRecorder.getRewriteEvents();
        // Record the new event
        newEventRecorder.record(rewriteEventsRecorder);
        // Grab the updated rewrite events
        final RewriteEvent[] updatedRewriteEvents = rewriteEventsRecorder.getRewriteEvents();
        // Validate state with the given expectation
        expectation.expect(originalRewriteEvents, updatedRewriteEvents, expectedNewRewriteEvent, rewriteEventIndex);
    }

    // -----------------* Invalid Merge Rewrite Events Test Cases *----------------- //

    @SuppressWarnings("unused") // Used by JUnitParams in `invalidMergeRewriteEventsTest` test case
    private Object testInValidMergeRewriteEventsParameters() {
        final DummyNode node = new DummyNode(0);
        final Recorder newParentInsertRecorder = new InsertRecorder(PARENT_NODE_2, NEW_CHILD_NODE_2, INSERT_I);
        final Recorder newParentReplaceRecorder = new ReplaceRecorder(PARENT_NODE_2, OLD_CHILD_NODE, NEW_CHILD_NODE_2, INSERT_I);
        final Recorder newParentRemoveRecorder = new InsertRecorder(PARENT_NODE_2, OLD_CHILD_NODE, INSERT_I);
        return new Object[] {
            // Insert cases
            new Object[] {ORIGINAL_INSERT_RECORDER, new InsertRecorder(PARENT_NODE_2, NEW_CHILD_NODE_2, INSERT_I)}, // Not the same parent
            new Object[] {ORIGINAL_INSERT_RECORDER, new ReplaceRecorder(PARENT_NODE_2, OLD_CHILD_NODE, NEW_CHILD_NODE_2, INSERT_I)}, // Not the same parent
            new Object[] {ORIGINAL_INSERT_RECORDER, new RemoveRecorder(PARENT_NODE_2, OLD_CHILD_NODE, INSERT_I)}, // Not the same parent
            // Replace cases
            new Object[] {ORIGINAL_REPLACE_RECORDER, new InsertRecorder(PARENT_NODE_2, NEW_CHILD_NODE_2, REPLACE_I)}, // Not the same parent
            new Object[] {ORIGINAL_REPLACE_RECORDER, new ReplaceRecorder(PARENT_NODE_2, OLD_CHILD_NODE, NEW_CHILD_NODE_2, REPLACE_I)}, // Not the same parent
            new Object[] {ORIGINAL_REPLACE_RECORDER, new RemoveRecorder(PARENT_NODE_2, OLD_CHILD_NODE, REPLACE_I)}, // Not the same parent
            // - [replace->replace] oldChildNode of the new event should be the same as the newChildNode of the original event for merging
            new Object[] {ORIGINAL_REPLACE_RECORDER, new ReplaceRecorder(PARENT_NODE, OLD_CHILD_NODE_2, NEW_CHILD_NODE_2, REPLACE_I)},
            // - [replace->remove] oldChildNode of the new event should be the same as the newChildNode of the original event for merging
            new Object[] {ORIGINAL_REPLACE_RECORDER, new RemoveRecorder(PARENT_NODE, OLD_CHILD_NODE_2, REPLACE_I)},
            // Remove cases
            new Object[] {ORIGINAL_REMOVE_RECORDER, new InsertRecorder(PARENT_NODE_2, NEW_CHILD_NODE_2, REMOVE_I)}, // Not the same parent
            new Object[] {ORIGINAL_REMOVE_RECORDER, new ReplaceRecorder(PARENT_NODE_2, OLD_CHILD_NODE, NEW_CHILD_NODE_2, REMOVE_I)}, // Not the same parent
            new Object[] {ORIGINAL_REMOVE_RECORDER, new RemoveRecorder(PARENT_NODE_2, OLD_CHILD_NODE, REMOVE_I)}, // Not the same parent
            // - Expecting fail as a remove event cannot be followed by a replace event.
            //      This would mean that an already removed node is then trying to be replaced, which makes no sense
            new Object[] {ORIGINAL_REMOVE_RECORDER, new ReplaceRecorder(PARENT_NODE, OLD_CHILD_NODE, NEW_CHILD_NODE_2, REMOVE_I)},
            // - Expecting fail as a remove event cannot be followed by another remove event.
            //      This would mean that an already removed node is then trying to be removed again, which makes no sense
            new Object[] {ORIGINAL_REMOVE_RECORDER, new RemoveRecorder(PARENT_NODE, OLD_CHILD_NODE, REMOVE_I)},
        };
    }

    @Test
    @Parameters(method = "testInValidMergeRewriteEventsParameters")
    public void testInValidMergeRewriteEvents(final Recorder originalEventRecorder, final Recorder newEventRecorder) {
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

    // xnow document
    // Expect: remove the original event
    private void expectRemovedOriginalRewriteEvent(final RewriteEvent[] rewriteEvents,
                                                   final RewriteEvent[] updatedRewriteEvents,
                                                   final int rewriteEventIndex) {
        // Check updated array size
        assertEquals(rewriteEvents.length - 1, updatedRewriteEvents.length);

        // Check updated array content
        int rewriteEventsIndex = 0;
        int updatedRewriteEventsIndex = 0;
        while (rewriteEventsIndex < rewriteEvents.length && updatedRewriteEventsIndex < updatedRewriteEvents.length) {
            if (rewriteEventsIndex == rewriteEventIndex && updatedRewriteEventsIndex == rewriteEventIndex) {
                rewriteEventsIndex++;
            } else {
                assertEquals(rewriteEvents[rewriteEventsIndex++], updatedRewriteEvents[updatedRewriteEventsIndex++]);
            }
        }
    }

    private interface Expectation {
        // xnow document
        void expect(RewriteEvent[] rewriteEvents, RewriteEvent[] updatedRewriteEvents, RewriteEvent expectedNewRewriteEvent, int rewriteEventIndex);
    }

    // xnow document
    // Expect: the new insert event has been inserted in the given index and the other events shifted to the right
    private static class InsertedNewRewriteEventExpectation implements Expectation {

        @Override
        public void expect(final RewriteEvent[] rewriteEvents,
                           final RewriteEvent[] updatedRewriteEvents,
                           final RewriteEvent expectedNewRewriteEvent,
                           final int rewriteEventIndex) {
            // Check updated array size is one more of the original size
            assertEquals(rewriteEvents.length + 1, updatedRewriteEvents.length);

            // Check updated array content has the new rewrite event
            int rewriteEventsIndex = 0;
            int updatedRewriteEventsIndex = 0;
            while (rewriteEventsIndex < rewriteEvents.length && updatedRewriteEventsIndex < updatedRewriteEvents.length) {
                if (rewriteEventsIndex == rewriteEventIndex && updatedRewriteEventsIndex == rewriteEventIndex) {
                    assertEquals(expectedNewRewriteEvent, updatedRewriteEvents[updatedRewriteEventsIndex++]);
                } else {
                    assertEquals(rewriteEvents[rewriteEventsIndex++], updatedRewriteEvents[updatedRewriteEventsIndex++]);
                }
            }
        }
    }

    // xnow document
    // Expect: replace the original event with the new expectedNewRewriteEvent, at the given rewriteEventIndex
    private static class ReplacedOriginalRewriteEventExpectation implements Expectation {

        @Override
        public void expect(final RewriteEvent[] rewriteEvents,
                           final RewriteEvent[] updatedRewriteEvents,
                           final RewriteEvent expectedNewRewriteEvent,
                           final int rewriteEventIndex) {
            // Check the updated array is of the same size
            assertEquals(rewriteEvents.length, updatedRewriteEvents.length);

            // Check that the array has been correctly updated
            for (int i = 0; i < updatedRewriteEvents.length; i++) {
                final RewriteEvent expectedRewriteEvent = i == rewriteEventIndex ? expectedNewRewriteEvent : rewriteEvents[i];
                assertEquals(expectedRewriteEvent, updatedRewriteEvents[i]);
            }
        }
    }

    // xnow document
    // Expect: remove the original event
    private static class RemovedOriginalRewriteEventExpectation implements Expectation {

        @Override
        public void expect(final RewriteEvent[] rewriteEvents,
                           final RewriteEvent[] updatedRewriteEvents,
                           final RewriteEvent expectedNewRewriteEvent,
                           final int rewriteEventIndex) {
            if (expectedNewRewriteEvent != null) {
                throw new IllegalArgumentException("Expecting `expectedNewRewriteEvent` to be null");
            }

            // Check updated array size
            assertEquals(rewriteEvents.length - 1, updatedRewriteEvents.length);

            // Check updated array content
            int rewriteEventsIndex = 0;
            int updatedRewriteEventsIndex = 0;
            while (rewriteEventsIndex < rewriteEvents.length && updatedRewriteEventsIndex < updatedRewriteEvents.length) {
                if (rewriteEventsIndex == rewriteEventIndex && updatedRewriteEventsIndex == rewriteEventIndex) {
                    rewriteEventsIndex++;
                } else {
                    assertEquals(rewriteEvents[rewriteEventsIndex++], updatedRewriteEvents[updatedRewriteEventsIndex++]);
                }
            }
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
