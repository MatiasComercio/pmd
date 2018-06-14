/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure;

import static net.sourceforge.pmd.util.GenericTokens.newEmptyToken;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.util.AbstractNodes;

public class Structure {
    private final List<SEInfo> structureInfo;

    private Structure(final Builder builder) {
        this.structureInfo = Collections.unmodifiableList(new LinkedList<>(builder.structureInfo));
    }

    public static Builder newBuilder() {
        return Builder.newInstance();
    }

    public List<SEInfo> getAllSEInfo() {
        return structureInfo;
    }

    public boolean matchSES(final Structure currNodeStructure) {
        final Iterator<SEInfo> thisIterator = structureInfo.iterator();
        final Iterator<SEInfo> otherIterator = currNodeStructure.structureInfo.iterator();
        boolean stillMatch = true;
        while (thisIterator.hasNext() && otherIterator.hasNext() && stillMatch) {
            if (!thisIterator.next().getSE().equals(otherIterator.next().getSE())) {
                stillMatch = false;
            }
        }
        return stillMatch && !thisIterator.hasNext() && !otherIterator.hasNext();
    }

    public int getSize() {
        return structureInfo.size();
    }

    public void sync(final AbstractNode node) {
        final SyncContext syncContext = SyncContext.newInstance(node);
        for (final SEInfo seInfo : structureInfo) {
            seInfo.sync(syncContext);
        }
        syncBoundTokens(node);
    }

    private void syncBoundTokens(final AbstractNode node) {
        final GenericToken newFirstToken;
        final GenericToken newLastToken;
        if (structureInfo.isEmpty()) {
            newFirstToken = newLastToken = newEmptyToken();
        } else {
            newFirstToken = structureInfo.get(0).getTokenRegion().getFirstToken();
            newLastToken = structureInfo.get(structureInfo.size() - 1).getTokenRegion().getLastToken();
        }
        AbstractNodes.updateBoundTokensAncestrally(node,
            node.jjtGetFirstToken(), node.jjtGetLastToken(),
            newFirstToken, newLastToken
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Structure structure = (Structure) o;

        return structureInfo.equals(structure.structureInfo);
    }

    @Override
    public int hashCode() {
        return structureInfo.hashCode();
    }

    public static final class Builder {
        private final List<SEInfo> structureInfo;

        private Builder() {
            this.structureInfo = new LinkedList<>();
        }

        private Builder(final List<SEInfo> structureInfo) {
            this.structureInfo = new LinkedList<>(structureInfo);
        }

        public static Builder newInstance() {
            return new Builder();
        }

        @SuppressWarnings("UnusedReturnValue") // Builder method.
        public Builder append(final SEInfo seInfo) {
            structureInfo.add(seInfo);
            return this;
        }

        public Builder duplicate() {
            return new Builder(structureInfo);
        }

        public Structure build() {
            return new Structure(this);
        }

        public SEInfo peekLastSEInfo() {
            return structureInfo.isEmpty() ? null : structureInfo.get(structureInfo.size() - 1);
        }
    }
}
