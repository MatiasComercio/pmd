/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.util;

import static org.apache.commons.lang3.StringUtils.isWhitespace;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

import net.sourceforge.pmd.lang.ast.GenericToken;

public abstract class GenericTokens {
    /**
     * Do the actual assembly.
     * Original: t1 -> t2.
     * To insert: t3 -> ... -> t4
     * Result: t1 -> t3 -> ... -> t4 -> t1
     */
    public static void assemble(final GenericToken t1, final GenericToken t3,
                                final GenericToken t4, final GenericToken t2) { // xdoc: improve: null checks; t3 & t4 may be the same, etc.
        if (t1 != null) {
            t1.setNext(t3);
        }
        if (t4 != null) {
            t4.setNext(t2);
        }
    }

    /**
     * Do the actual assembly.
     * Original: t1 -> t3 -> ... -> t4 -> t1
     * To remove: t3 -> ... -> t4
     * Result: t1 -> t2.
     */
    // Suppressed `unused` just for completeness & similar contract with `assemble` method.
    public static void disassemble(final GenericToken t1, @SuppressWarnings("unused") final GenericToken t3,
                                   final GenericToken t4, final GenericToken t2) { // xdoc: improve: null checks; t3 & t4 may be the same, etc.
        if (t1 != null) {
            t1.setNext(t2);
        }
        // else: fc is the first token of the list => we only need to update the nodes' bound tokens
        t4.setNext(null);
    }

    public static GenericToken newEmptyToken() {
        return new EmptyToken();
    }

    public static boolean isEmptyToken(final GenericToken token) {
        return token instanceof EmptyToken; // Other possibility: return token.getImage() == null.
    }

    // xdoc: only insert the given special if it does not already exit as the 'first seen (displayed)' special token. create a `*Forced` method to skip this validation.
    public static void prependSpecial(final GenericToken token, final GenericToken newSpecial) {
        GenericToken prev = null;
        GenericToken curr = token;
        while (curr.getSpecialToken() != null) {
            prev = curr;
            curr = curr.getSpecialToken();
        }
        if (prev == null) { // => curr = token => this token has not even one special token => set the new one.
            curr.setSpecialToken(newSpecial);
        } else {
            final String firstSpecialImage = prev.getSpecialToken().getImage();
            final String newSpecialImage = newSpecial.getImage();
            // If they are different & they are not both whitespaces => set the new special token.
            if (!Objects.equals(firstSpecialImage, newSpecialImage)
                && !(isWhitespace(firstSpecialImage) && isWhitespace(newSpecialImage))) {
                curr.setSpecialToken(newSpecial);
            }
        }
    }

    public static String stringify(final GenericToken firstInclusiveToken, final GenericToken lastInclusiveToken) {
        final StringBuilder sb = new StringBuilder();
        if (firstInclusiveToken == null) { // No tokens.
            return sb.toString();
        }

        GenericToken curr = firstInclusiveToken;
        while (curr != lastInclusiveToken) {
            if (curr.getSpecialToken() != null) {
                printSpecialToken(sb, curr);
            }
            sb.append(curr.getImage());
            curr = curr.getNext();
        }
        sb.append(curr.getImage()); // Print last token

        return sb.toString();
    }

    private static void printSpecialToken(final StringBuilder sb, final GenericToken curr) {
        final Deque<String> specialTokens =new LinkedList<>();
        GenericToken special = curr.getSpecialToken();
        while (special != null) {
            specialTokens.push(special.getImage());
            special = special.getSpecialToken();
        }
        while (!specialTokens.isEmpty()) {
            sb.append(specialTokens.pop());
        }
    }

    private static final class EmptyToken implements GenericToken {
        private static final int NO_POSITION = -1; // All begin/end line/column should return this number.
        private static final int KIND = -1;

        private GenericToken next;
        private GenericToken special;

        @Override
        public GenericToken getNext() {
            return next;
        }

        @Override
        public void setNext(final GenericToken token) {
            this.next = token;
        }

        @Override
        public GenericToken getPreviousComment() {
            return special;
        }

        @Override
        public GenericToken getSpecialToken() {
            return special;
        }

        @Override
        public void setSpecialToken(final GenericToken token) {
            this.special = token;
        }

        @Override
        public String getImage() {
            return ""; // EmptyToken has no image.
        }

        @Override
        public int getBeginLine() {
            return NO_POSITION;
        }

        @Override
        public int getEndLine() {
            return NO_POSITION;
        }

        @Override
        public int getBeginColumn() {
            return NO_POSITION;
        }

        @Override
        public int getEndColumn() {
            return NO_POSITION;
        }

        @Override
        public boolean is(final int kind) {
            return KIND == kind;
        }
    }
}
