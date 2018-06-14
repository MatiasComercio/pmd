/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;

// TODO: unify notation on first/last parent/child tokens.
public abstract class AbstractNodes { // ximportant: should be renamed to PmdNode (or Node) when get/set first/last token are moved to the interface.
    public static void updateBoundTokensAncestrally(final AbstractNode node,
                                                    final GenericToken oldFirstToken, final GenericToken oldLastToken,
                                                    final GenericToken newFirstToken, final GenericToken newLastToken) {
        // Instance equal is OK when dealing with tokens in this method.
        final GenericToken firstToken = node.jjtGetFirstToken();
        final GenericToken lastToken = node.jjtGetLastToken();

        /*
         * If I have the same first/last token than my child, I have to assign myself its same new first/last token.
         * If not, I have to keep my current token.
         */
        final GenericToken ownNewFirstToken = firstToken == oldFirstToken ? newFirstToken : firstToken;
        final GenericToken ownNewLastToken = lastToken == oldLastToken ? newLastToken : lastToken;
        if (firstToken != ownNewFirstToken || lastToken != ownNewLastToken) {
            /*
             * If any of my first/last child tokens have changed, my parent (if any) should be notified, as he may have
             *  the same first/last tokens as me, and if so, he would have to change his first/last tokens too.
             */
            final Node parent = node.jjtGetParent();
            if (parent != null) {
                final AbstractNode aParent = node.requireAbstractNode(parent);
                updateBoundTokensAncestrally(aParent, oldFirstToken, oldLastToken, ownNewFirstToken, ownNewLastToken);
            }

            // Assign our new bound tokens.
            node.jjtSetFirstToken(ownNewFirstToken);
            node.jjtSetLastToken(ownNewLastToken);
        }
    }
}
