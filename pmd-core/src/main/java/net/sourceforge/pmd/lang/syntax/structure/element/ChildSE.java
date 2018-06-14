/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure.element;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;

public final class ChildSE extends AbstractSingleMatchingStateSE {
    private final Class<? extends Node> validChildClass;

    private ChildSE(final Class<? extends Node> validChildClass) {
        this.validChildClass = validChildClass;
    }

    public static ChildSE newInstance(final Class<? extends Node> validChildClass) {
        return new ChildSE(validChildClass);
    }

    @Override
    public TokenRegion sync(final SyncContext syncContext,
                            final StructureElement oldSE,
                            final TokenRegion oldTokenRegion) {
        final AbstractNode child = syncContext.nextChild();
        if (child == null || isInvalidChildClass(child.getClass())) {
            // implNote: if here, check Parser#parseChildren
            throw new IllegalStateException("Node does not match structure. Expected child not found.");
        }

        final GenericToken childFirstToken = child.jjtGetFirstToken();
        final GenericToken childLastToken = child.jjtGetLastToken();
        final GenericToken oldFirstToken = oldTokenRegion.getFirstToken();
        final GenericToken oldLastToken = oldTokenRegion.getLastToken();
        if (this.equals(oldSE) && childFirstToken.equals(oldFirstToken) && childLastToken.equals(oldLastToken)) {
            return oldTokenRegion; // We are representing the exact same element.
        }

        // We need to create a new region for the matching child tokens.
        return TokenRegion.newInstance(childFirstToken, childLastToken);
    }

    @Override
    public boolean shouldMatch(final ParsePolicy policy, final ParsedValue value) {
        return policy == ParsePolicy.COMPLETE || policy == ParsePolicy.CHILD_ONLY;
    }

    @Override
    public boolean doesMatch(final GenericToken parsedToken) {
        return false;
    }

    @Override
    public boolean doesMatch(final AbstractNode parsedChild) {
        return !isInvalidChildClass(parsedChild.getClass());
    }

    private boolean isInvalidChildClass(final Class<? extends Node> childClass) {
        return !validChildClass.equals(childClass);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " `" + validChildClass.getSimpleName() + "`";
    }
}
