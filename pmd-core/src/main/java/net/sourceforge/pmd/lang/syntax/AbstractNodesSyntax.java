/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.meta.NodeMetaInfo;
import net.sourceforge.pmd.lang.meta.NodesMetaInfo;
import net.sourceforge.pmd.lang.syntax.parser.grammar.Symbol;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operator.AlternationSym;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operator.ConcatenationSym;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operator.OneOrMoreSym;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operator.ZeroOrMoreSym;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operator.ZeroOrOneSym;
import net.sourceforge.pmd.lang.syntax.structure.element.ChildSE;

// T is the base type of all nodes syntax
// C is a specialized type of node syntax of type T
public abstract class AbstractNodesSyntax<T extends AbstractNode> implements NodesSyntax<T> {
    private final Map<Class<? extends T>, NodeSyntax<? extends T>> syntaxByNode;
    private final NodesMetaInfo<T> nodesMetaInfo;

    protected AbstractNodesSyntax(final NodesMetaInfo<T> nodesMetaInfo) {
        syntaxByNode = new HashMap<>();
        this.nodesMetaInfo = nodesMetaInfo;
    }

    @Override
    public final <C extends T> NodeSyntax<C> getNodeSyntax(final Class<C> nodeClass) {
        //noinspection unchecked // We are storing keys of Class<C> with values of NodeSyntax<C>.
        return (NodeSyntax<C>) syntaxByNode.get(nodeClass);
    }

    protected <C extends T> void define(final Class<C> nodeClazz, final Symbol... symbols) {
        syntaxByNode.put(nodeClazz, NodeSyntaxImpl.newInstance(metaInfo(nodeClazz), symbols));
    }

    protected final ChildSE child(final Class<? extends T> validChildClass) {
        return ChildSE.newInstance(validChildClass);
    }

    protected ZeroOrOneSym zeroOrOne(final Symbol... symbols) {
        return ZeroOrOneSym.newInstance(symbols);
    }

    protected ZeroOrMoreSym zeroOrMore(final Symbol... symbols) {
        return ZeroOrMoreSym.newInstance(symbols);
    }

    protected OneOrMoreSym oneOrMore(final Symbol... symbols) {
        return OneOrMoreSym.newInstance(symbols);
    }

    protected AlternationSym or(final Symbol... symbols) {
        return AlternationSym.newInstance(symbols);
    }

    protected ConcatenationSym seq(final Symbol... symbols) {
        return ConcatenationSym.newInstance(symbols);
    }

    private <C extends T> NodeMetaInfo<C> metaInfo(final Class<C> nodeClazz) {
        return nodesMetaInfo.getInstance(nodeClazz);
    }
}
