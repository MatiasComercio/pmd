/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax;

import java.util.Objects;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.meta.NodeMetaInfo;
import net.sourceforge.pmd.lang.syntax.structure.element.StructureElement;
import net.sourceforge.pmd.lang.syntax.structure.element.TokenSE;

public class OldNodeStructureImpl<T extends AbstractNode>  {
    private final NodeMetaInfo<T> nodeMetaInfo; // Not null.
    private final StructureElement[] structureElements;

    private OldNodeStructureImpl(final NodeMetaInfo<T> nodeMetaInfo, final StructureElement[] structureElements) {
        this.nodeMetaInfo = nodeMetaInfo;
        this.structureElements = structureElements;
    }

    public static <T extends AbstractNode> OldNodeStructureImpl<T> newInstance(final NodeMetaInfo<T> nodeMetaInfo,
                                                                               final StructureElement... structureElements) {
        return new OldNodeStructureImpl<>(Objects.requireNonNull(nodeMetaInfo), structureElements);
    }

    @SuppressWarnings("unchecked") // This is for "SafeVarargs". Not using annotation as this method may be overridden.
    public <C extends Node> T newInstance(final C... children) { // xdoc: specify that these children are added in the given order.
        // Create the new node.
        final T newNode = nodeMetaInfo.newNode();
        if (newNode == null) {
            return null;
        }

        // Build its the token list.
        GenericToken firstToken = null;
        GenericToken prevToken = null;
        GenericToken currToken = null;
        for (final StructureElement currSE : structureElements) {
            if (!(currSE instanceof TokenSE)) { // ximprove: the way we are doing this.
                continue;
            }

            currToken = ((TokenSE) currSE).newToken();
            if (prevToken != null) {
                prevToken.setNext(currToken);
            }

            if (firstToken == null) {
                firstToken = currToken;
            }

            prevToken = currToken;
        }
        final GenericToken lastToken = currToken;

        // Assign its first and last tokens.
        newNode.jjtSetFirstToken(firstToken);
        newNode.jjtSetLastToken(lastToken);

        // Insert its children.
        for (int childIndex = 0; childIndex < children.length; childIndex ++) {
            final C child = children[childIndex];
            newNode.addChild(childIndex, child);
        }

        // Return the new node with all the given children :D
        return newNode;
    }
}
