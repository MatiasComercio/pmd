package net.sourceforge.pmd.autofix.rewriteevents;

import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Before;
import org.junit.Test;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createInsertRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createRemoveRewriteEvent;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventFactory.createReplaceRewriteEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    private static final Node OLD_CHILD_NODE = DummyNode.newInstance();
    private static final Node NEW_CHILD_NODE = DummyNode.newInstance();
    private static final int INSERT_I = 0;
    private static final int REPLACE_I = 1;
    private static final int REMOVE_I = 2;

    private static final RewriteEvent INSERT_REWRITE_EVENT = createInsertRewriteEvent(PARENT_NODE, INSERT_I, NEW_CHILD_NODE);
    private static final RewriteEvent REPLACE_REWRITE_EVENT = createReplaceRewriteEvent(PARENT_NODE, REPLACE_I, OLD_CHILD_NODE, NEW_CHILD_NODE);
    private static final RewriteEvent REMOVE_REWRITE_EVENT = createRemoveRewriteEvent(PARENT_NODE, REMOVE_I, OLD_CHILD_NODE);

    private RewriteEventsRecorder rewriteEventsRecorder;

    @Before
    public void initializeRewriteEvents() {
        rewriteEventsRecorder = new RewriteEventsRecorderImpl();
    }

    @Test
    public void testRecordRemove() {
        // Do the actual record
        rewriteEventsRecorder.recordRemove(PARENT_NODE, OLD_CHILD_NODE, REMOVE_I);

        assertTrue(rewriteEventsRecorder.hasRewriteEvents());
        assertEquals(REMOVE_REWRITE_EVENT, rewriteEventsRecorder.getRewriteEvents()[REMOVE_I]);
    }
}
