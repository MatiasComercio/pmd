/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure.element;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;

public abstract class AbstractTokenSE extends AbstractSingleMatchingStateSE implements TokenSE {

    protected AbstractTokenSE() { }

    @Override
    public TokenRegion sync(final SyncContext syncContext,
                            final StructureElement oldSE,
                            final TokenRegion oldTokenRegion) {
        // We are representing the same token => reuse old region :D
        if (this.equals(oldSE) && oldTokenRegion.stringify().equals(getImage())) {
            return oldTokenRegion;
        }

        /*
         * It may be the same oldSE but have a different image, for example, when the oldTokenRegion is empty.
         *  This may happen when this node has been dynamically created.
         *
         * In any case, if any of the above conditions is not met,
         *  we MUST create a new region with a new own token.
         */
        final GenericToken newToken = newToken();
        return TokenRegion.newInstance(newToken, newToken);
    }

    @Override
    public boolean shouldMatch(final ParsePolicy policy, final ParsedValue value) {
        return policy == ParsePolicy.COMPLETE;
    }

    @Override
    public boolean doesMatch(final GenericToken parsedToken) {
        return parsedToken.getImage().equals(getImage());
    }

    @Override
    public boolean doesMatch(final AbstractNode parsedChild) {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " `" + getImage() + "`";
    }
}
