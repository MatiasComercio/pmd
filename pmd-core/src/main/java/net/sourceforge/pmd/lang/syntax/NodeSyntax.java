/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.syntax.structure.Structure;

// xTODO: Extract token methods to an interface & migrate this T type to extend from that interface instead.
public interface NodeSyntax<T extends AbstractNode> {
    @SuppressWarnings("unchecked") // This is for "SafeVarargs" check.
    <C extends Node> T newInstance(final C... children);
    Structure scan(T node); // xdoc: should be performed prior to any modification over the given node.
    Structure sync(T node);
}
