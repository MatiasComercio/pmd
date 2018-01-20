package net.sourceforge.pmd.autofix.rewriteevents;

import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.INSERT;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REMOVE;
import static net.sourceforge.pmd.autofix.rewriteevents.RewriteEventType.REPLACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import net.sourceforge.pmd.lang.ast.DummyNode;
import net.sourceforge.pmd.lang.ast.Node;
import org.junit.Test;

public class RewriteEventFactoryTest {
    private static final Node PARENT_NODE = DummyNode.newInstance();
    private static final Node OLD_CHILD_NODE = DummyNode.newInstance();
    private static final Node NEW_CHILD_NODE = DummyNode.newInstance();
    private static final int CHILD_INDEX = 1;

    @Test
    public void testCreateInsertRewriteEvent() {
        final RewriteEvent insertRewriteEvent = RewriteEventFactory.createInsertRewriteEvent(PARENT_NODE, CHILD_INDEX, NEW_CHILD_NODE);
        assertEquals(INSERT, insertRewriteEvent.getRewriteEventType());
        assertEquals(PARENT_NODE, insertRewriteEvent.getParentNode());
        assertNull(insertRewriteEvent.getOldChildNode());
        assertEquals(NEW_CHILD_NODE, insertRewriteEvent.getNewChildNode());
        assertEquals(CHILD_INDEX, insertRewriteEvent.getChildNodeIndex());
    }

    @Test
    public void testCreateRemoveRewriteEvent() {
        final RewriteEvent insertRewriteEvent = RewriteEventFactory.createRemoveRewriteEvent(PARENT_NODE, CHILD_INDEX, OLD_CHILD_NODE);
        assertEquals(REMOVE, insertRewriteEvent.getRewriteEventType());
        assertEquals(PARENT_NODE, insertRewriteEvent.getParentNode());
        assertEquals(OLD_CHILD_NODE, insertRewriteEvent.getOldChildNode());
        assertNull(insertRewriteEvent.getNewChildNode());
        assertEquals(CHILD_INDEX, insertRewriteEvent.getChildNodeIndex());
    }

    @Test
    public void testCreateReplaceRewriteEvent() {
        final RewriteEvent insertRewriteEvent = RewriteEventFactory.createReplaceRewriteEvent(PARENT_NODE, CHILD_INDEX, OLD_CHILD_NODE, NEW_CHILD_NODE);
        assertEquals(REPLACE, insertRewriteEvent.getRewriteEventType());
        assertEquals(PARENT_NODE, insertRewriteEvent.getParentNode());
        assertEquals(OLD_CHILD_NODE, insertRewriteEvent.getOldChildNode());
        assertEquals(NEW_CHILD_NODE, insertRewriteEvent.getNewChildNode());
        assertEquals(CHILD_INDEX, insertRewriteEvent.getChildNodeIndex());
    }
}
