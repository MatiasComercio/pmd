/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.context;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.syntax.structure.SEInfo;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;
import net.sourceforge.pmd.util.GenericTokens;

public class SyncContext {
    private final AbstractNode aNode;
    private final Iterator<SEInfo> oldSEInfoIterator;

    private SEInfo nextOldSEInfo;
    private int nextChildIndex;
    private GenericToken prevToken;
    private GenericToken nextToken;

    private SyncContext(final AbstractNode aNode) {
        this.aNode = aNode;
        final List<SEInfo> oldSESInfo = aNode.getStructure().getAllSEInfo();

        final GenericToken firstToken = aNode.jjtGetFirstToken();
        final GenericToken lastToken = aNode.jjtGetLastToken();
        this.prevToken = aNode.getPrevToken(firstToken);
        this.nextToken = lastToken.getNext();

        // Remove all node's region.
        GenericTokens.disassemble(prevToken, firstToken, lastToken, nextToken);

        this.oldSEInfoIterator = oldSESInfo.iterator();
        this.nextOldSEInfo = safeNextSEInfo();
        this.nextChildIndex = 0;
    }

    public static SyncContext newInstance(final AbstractNode aNode) {
        return new SyncContext(requireNonNull(aNode));
    }

    public AbstractNode getNode() {
        return aNode;
    }

    public boolean hasNextOldSEInfo() {
        return nextOldSEInfo != null;
    }

    public SEInfo nextOldSEInfo() {
        if (!hasNextOldSEInfo()) return null;

        final SEInfo currOldSEInfo = nextOldSEInfo;
        nextOldSEInfo = safeNextSEInfo();
        return currOldSEInfo;
    }

    public SEInfo peekNextOldSEInfo() {
        return nextOldSEInfo;
    }

    private SEInfo safeNextSEInfo() {
        return oldSEInfoIterator.hasNext() ? oldSEInfoIterator.next() : null;
    }

    public AbstractNode nextChild() {
        final AbstractNode nextChild = peekNextChild();
        if (nextChild != null) {
            nextChildIndex ++;
        }
        return nextChild;
    }

    public AbstractNode peekNextChild() {
        return nextChildIndex < aNode.jjtGetNumChildren() ?
            aNode.requireAbstractNode(aNode.jjtGetChild(nextChildIndex)) : null;
    }

    public void insert(final TokenRegion newTokenRegion) {
        // Insert this region before nextToken & update the prevToken.
        final GenericToken firstToken = requireNonNull(newTokenRegion.getFirstToken());
        final GenericToken lastToken = requireNonNull(newTokenRegion.getLastToken());
        GenericTokens.assemble(prevToken, firstToken, lastToken, nextToken);
        prevToken = lastToken; // Which would equal nextToken.prev (if this method existed).
    }
}
