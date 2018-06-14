/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.meta.NodeMetaInfo;
import net.sourceforge.pmd.lang.syntax.parser.Parser;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operator.ConcatenationSym;
import net.sourceforge.pmd.lang.syntax.structure.Structure;
import net.sourceforge.pmd.util.GenericTokens;

public class NodeSyntaxImpl<T extends AbstractNode> implements NodeSyntax<T> {
    private final Object lock = new Object();
    private final NodeMetaInfo<T> nodeMetaInfo;
    private final Symbol symbol;
    private Parser parser;

    private NodeSyntaxImpl(final NodeMetaInfo<T> nodeMetaInfo, final Symbol symbol) {
        this.nodeMetaInfo = nodeMetaInfo;
        this.symbol = symbol;
        this.parser = null; // Compute later ONLY if needed (i.e., only if ever used).
    }

    public static <T extends AbstractNode> NodeSyntaxImpl<T> newInstance(final NodeMetaInfo<T> nodeMetaInfo,
                                                                         final Symbol symbol) {
        return new NodeSyntaxImpl<>(requireNonNull(nodeMetaInfo), requireNonNull(symbol));
    }

    /** Group all this symbols as a sequence, i.e., it concatenates them. */
    public static <T extends AbstractNode> NodeSyntaxImpl<T> newInstance(final NodeMetaInfo<T> nodeMetaInfo,
                                                                         final Symbol... symbols) {
        return newInstance(nodeMetaInfo, ConcatenationSym.newInstance(symbols));
    }

    @SafeVarargs
    // xdoc: specify that these children are added in the given order.
    public final <C extends Node> T newInstance(final C... children) {
        // Create the new node.
        final T newNode = nodeMetaInfo.newNode();
        if (newNode == null) {
            return null;
        }

        // Set first/last token as emptyTokens.
        final GenericToken emptyToken = GenericTokens.newEmptyToken();
        newNode.jjtSetFirstToken(emptyToken);
        newNode.jjtSetLastToken(emptyToken);

        // Set its structure so it can be lately synced.
        newNode.setStructure(scanNew(newNode));

        // Insert its children.
        for (int childIndex = 0; childIndex < children.length; childIndex ++) {
            final C child = children[childIndex];
            newNode.addChild(childIndex, child);
        }

        // Activate the sync required flag in case none children have been added (i.e.: image node only).
        newNode.syncRequired();

        // Return the new node with all the given children :D
        return newNode;
    }

    private Structure scanNew(final T node) {
        return pickNewStructure(node.getStructure(), ensureParserPresent().parseNew(node));
    }

    @Override
    public Structure scan(final T node) {
        return ensureParserPresent().parseComplete(node);
    }

    @Override
    public Structure sync(final T node) {
        final Set<Structure> possibleNewStructures = ensureParserPresent().parseChildren(node);
        final Structure newStructure = pickNewStructure(node.getStructure(), possibleNewStructures);
        newStructure.sync(node);
        return newStructure;
    }

    private Parser ensureParserPresent() {
        if (parser == null) {
            // To support concurrent use (e.g.: multiple files being fixed simultaneously).
            synchronized (lock) {
                if (parser == null) { // Enhanced check just to check if previous lock holder initialized the parser.
                    parser = Parser.newInstance(symbol);
                }
            }
        }
        return parser;
    }

    private Structure pickNewStructure(final Structure currNodeStructure, final Set<Structure> possibleStructures) {
        final Iterator<Structure> it = possibleStructures.iterator();
        boolean currStructurePresent = false;
        Structure newStructure = null;
        int minStructureSize = Integer.MAX_VALUE;

        // Try to select the same structure as the current one, if possible; if not, any with the min size.
        while (it.hasNext() && !currStructurePresent) {
            final Structure possibleStructure = it.next();
            if (currNodeStructure != null && possibleStructure.matchSES(currNodeStructure)) {
                newStructure = possibleStructure;
                currStructurePresent = true;
            } else {
                final int structureSize = possibleStructure.getSize();
                if (structureSize < minStructureSize) {
                    minStructureSize = structureSize;
                    newStructure = possibleStructure;
                }
            }
        }

        if (newStructure == null) {
            throw new IllegalStateException("newStructure == null, so possibleStructures set was empty.");
        }

        return newStructure;
    }
}
