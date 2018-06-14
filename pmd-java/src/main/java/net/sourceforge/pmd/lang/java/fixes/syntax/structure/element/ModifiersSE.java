/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.fixes.syntax.structure.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.java.ast.AccessNode;
import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;
import net.sourceforge.pmd.lang.syntax.structure.element.AbstractReentrantMatchingStateSE;
import net.sourceforge.pmd.lang.syntax.structure.element.StructureElement;
import net.sourceforge.pmd.util.GenericTokens;

/** We know that modifiers come before the child which is assigned these modifiers. */
public class ModifiersSE extends AbstractReentrantMatchingStateSE {
    private ModifiersSE() { }

    public static ModifiersSE newInstance() {
        return new ModifiersSE();
    }

    @Override
    public TokenRegion sync(final SyncContext syncContext,
                            final StructureElement oldSE,
                            final TokenRegion oldTokenRegion) {

        // Recall that modifiers come before the child which is assigned these modifiers.
        final AbstractNode child = syncContext.peekNextChild();
        if (!(child instanceof AccessNode)) { // non-null check included in the instanceof
            throw new IllegalStateException("Node is not well-formed. Expected child not found.");
        }
        final AccessNode accessNode = (AccessNode) child;
        final Set<AccessNode.Modifier> sortedEnabledModifiers = AccessNode.Modifier.getSortedEnabledModifiers(
            accessNode.getModifiers()
        );

        final List<GenericToken> presentEnabledModifierTokens = oldTokenRegion.isEmpty() // No present modifiers.
            ? Collections.<GenericToken>emptyList()
            : filterPresentEnabledModifierTokens(
                oldTokenRegion.getFirstToken(), oldTokenRegion.getLastToken(), sortedEnabledModifiers
            );

        return linkAllEnabledModifierTokens(presentEnabledModifierTokens, sortedEnabledModifiers);
    }

    @Override
    public boolean shouldMatch(final ParsePolicy policy, final ParsedValue value) {
        return policy == ParsePolicy.COMPLETE;
    }

    @Override
    public boolean doesMatch(final GenericToken parsedToken) {
        return AccessNode.Modifier.getFromToken(parsedToken) != null;
    }

    @Override
    public boolean doesMatch(final AbstractNode parsedChild) {
        return false;
    }

    // xdoc: we are removing present modifiers from the sortedEnabled to prioritize existing tokens to have a minimal impact in code changes.
    private List<GenericToken> filterPresentEnabledModifierTokens(final GenericToken firstToken,
                                                                  final GenericToken lastToken,
                                                                  final Set<AccessNode.Modifier> sortedEnabledModifiers) {
        final List<GenericToken> presentEnabledModifierTokens = new ArrayList<>(sortedEnabledModifiers.size());

        // Choose those modifiers that are still enabled & grab their tokens to have a minimal impact in code changes.
        GenericToken currToken = firstToken;
        final GenericToken lastExclusiveToken = lastToken.getNext();
        while (currToken != lastExclusiveToken) {
            final AccessNode.Modifier modifier = AccessNode.Modifier.getFromToken(currToken);
            if (modifier == null) {
                throw new IllegalStateException("Expected modifier token inside an old modifiers region. Check code.");
            }

            // This modifier is still enabled => add it in this order.
            if (sortedEnabledModifiers.remove(modifier)) {
                presentEnabledModifierTokens.add(currToken);
            }

            final GenericToken prevToken = currToken;
            currToken = currToken.getNext();
            // Unlink each modifier token as: still enabled will be linked again; now disabled should be unlinked.
            prevToken.setNext(null);
        }

        return presentEnabledModifierTokens;
    }

    private TokenRegion linkAllEnabledModifierTokens(final List<GenericToken> currModifiersTokens,
                                                     final Set<AccessNode.Modifier> newModifiers) {
        /*
         * Link (i.e., concatenate) all enabled modifiers, i.e., currModifiersTokens & newModifiers,
         * in the best possible way by respecting the currently present modifiers order.
         */
        final ModifiersContext ctx = new ModifiersContext(currModifiersTokens, newModifiers);

        while (ctx.hasCurrModifier() && ctx.hasNewModifier()) {
            if (ctx.currModifier.getPosition() < ctx.newModifier.getPosition()) {
                ctx.linkCurrModifier();
            } else {
                ctx.linkNewModifier();
            }
        }

        while (ctx.hasCurrModifier()) {
            ctx.linkCurrModifier();
        }

        while (ctx.hasNewModifier()) {
            ctx.linkNewModifier();
        }

        return ctx.buildModifiersRegion();
    }

    private static final class ModifiersContext {
        private final Iterator<GenericToken> currModifiersTokens;
        private final Iterator<AccessNode.Modifier> newModifiers;

        private GenericToken currModifierToken;
        private AccessNode.Modifier currModifier;
        private AccessNode.Modifier newModifier;
        private GenericToken firstToken;
        private GenericToken lastToken;

        /* package-private */ ModifiersContext(final List<GenericToken> currModifiersTokens,
                                               final Set<AccessNode.Modifier> newModifiers) {
            this.currModifiersTokens = currModifiersTokens.iterator();
            this.newModifiers = newModifiers.iterator();
            nextCurrModifier();
            nextNewModifier();
            this.firstToken = GenericTokens.newEmptyToken();
            this.lastToken = firstToken;
        }

        /* package-private */ boolean hasCurrModifier() {
            return currModifier != null;
        }

        /* package-private */ boolean hasNewModifier() {
            return newModifier != null;
        }

        /* package-private */ void linkCurrModifier() {
            append(currModifierToken);
            nextCurrModifier();
        }

        /* package-private */ void linkNewModifier() {
            append(newModifier.getNewToken());
            nextNewModifier();
        }

        /* package-private */ TokenRegion buildModifiersRegion() {
            return TokenRegion.newInstance(firstToken, lastToken);
        }

        private void append(final GenericToken currToken) {
            if (GenericTokens.isEmptyToken(firstToken)) {
                firstToken = currToken;
            }

            // Link tokens.
            if (!GenericTokens.isEmptyToken(lastToken)) {
                lastToken.setNext(currToken);
            }
            lastToken = currToken;

            // Prepend a space, if required.
            GenericTokens.prependSpecial(currToken, JavaTokenSE.SPACE.newToken());
        }

        private void nextCurrModifier() {
            currModifierToken = currModifiersTokens.hasNext() ? currModifiersTokens.next() : null;
            currModifier = currModifierToken == null ? null : AccessNode.Modifier.getFromToken(currModifierToken);
        }

        private void nextNewModifier() {
            newModifier = newModifiers.hasNext() ? newModifiers.next() : null;
        }
    }
}
