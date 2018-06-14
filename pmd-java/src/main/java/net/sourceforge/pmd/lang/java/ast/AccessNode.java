/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * This interface captures Java access modifiers.
 */
public interface AccessNode extends Node {
    /*
     * Modifiers' order assigned as suggested by Checkstyle at: http://checkstyle.sourceforge.net/config_modifier.html
     */
    enum Modifier {
        PUBLIC(0x0001, JavaParserConstants.PUBLIC, 1),
        PROTECTED(0x0002, JavaParserConstants.PROTECTED, 2),
        PRIVATE(0x0004, JavaParserConstants.PRIVATE, 3),
        ABSTRACT(0x0008, JavaParserConstants.ABSTRACT, 4),
        STATIC(0x0010, JavaParserConstants.STATIC, 6),
        FINAL(0x0020, JavaParserConstants.FINAL, 7),
        SYNCHRONIZED(0x0040, JavaParserConstants.SYNCHRONIZED, 10),
        NATIVE(0x0080, JavaParserConstants.NATIVE, 11),
        TRANSIENT(0x0100, JavaParserConstants.TRANSIENT, 8),
        VOLATILE(0x0200, JavaParserConstants.VOLATILE, 9),
        STRICTFP(0x1000, JavaParserConstants.STRICTFP, 12),
        DEFAULT(0x2000, JavaParserConstants.DEFAULT, 5);

        private static final Comparator<Modifier> POSITION_COMPARATOR = new Comparator<Modifier>() {
            @Override
            public int compare(final Modifier m1, final Modifier m2) {
                return m1.position - m2.position;
            }
        };
        private static final Map<Integer, Modifier> MASK_TO_MODIFIER;
        private static final Map<String, Modifier> IMAGE_TO_MODIFIER;
        private static final Set<Modifier> MODIFIERS_SORTED_BY_POSITION;


        static {
            final Map<Integer, Modifier> maskToModifier = new HashMap<>();
            final Map<String, Modifier> imageToModifier = new HashMap<>();
            final Set<Modifier> modifiersSortedByPosition = new TreeSet<>(POSITION_COMPARATOR);

            for (final Modifier modifier : values()) {
                maskToModifier.put(modifier.mask, modifier);
                imageToModifier.put(modifier.image, modifier);
                modifiersSortedByPosition.add(modifier);
            }

            MASK_TO_MODIFIER = Collections.unmodifiableMap(maskToModifier);
            IMAGE_TO_MODIFIER = Collections.unmodifiableMap(imageToModifier);
            MODIFIERS_SORTED_BY_POSITION = Collections.unmodifiableSet(modifiersSortedByPosition);
        }

        private final int mask;
        private final int tokenType;
        private final int position;
        private final String image;

        Modifier(final int mask, final int tokenType, final int position) {
            this.mask = mask;
            this.tokenType = tokenType;
            this.position = position;
            this.image = TokenFactory.INSTANCE.imageFor(tokenType);
        }

        /** xdoc: sorted by position */
        public static Set<Modifier> getSortedEnabledModifiers(final int modifierMasks) {
            final Set<Modifier> enabledModifiers = new TreeSet<>(POSITION_COMPARATOR);

            for (final Modifier modifier : MODIFIERS_SORTED_BY_POSITION) {
                if (isModifier(modifierMasks, modifier.mask)) {
                    enabledModifiers.add(modifier);
                }
            }

            return enabledModifiers;
        }

        private static boolean isModifier(final int modifiers, final int mask) {
            return (modifiers & mask) == mask;
        }

        public int getMask() {
            return mask;
        }

        public int getTokenType() {
            return tokenType;
        }

        public int getPosition() {
            return position;
        }

        public static Modifier getFromMask(final int mask) {
            return MASK_TO_MODIFIER.get(mask);
        }

        public static Modifier getFromToken(final GenericToken currentToken) {
            return getFromImage(currentToken.getImage());
        }

        public static Modifier getFromImage(final String image) {
            return IMAGE_TO_MODIFIER.get(image);
        }

        public static Set<Modifier> getModifiersSortedByPosition() {
            return MODIFIERS_SORTED_BY_POSITION;
        }

        public Token getNewToken() {
            return TokenFactory.INSTANCE.newToken(tokenType);
        }

        public String getImage() {
            return image;
        }
    }

    int PUBLIC = Modifier.PUBLIC.getMask();
    int PROTECTED = Modifier.PROTECTED.getMask();
    int PRIVATE = Modifier.PRIVATE.getMask();
    int ABSTRACT = Modifier.ABSTRACT.getMask();
    int STATIC = Modifier.STATIC.getMask();
    int FINAL = Modifier.FINAL.getMask();
    int SYNCHRONIZED = Modifier.SYNCHRONIZED.getMask();
    int NATIVE = Modifier.NATIVE.getMask();
    int TRANSIENT = Modifier.TRANSIENT.getMask();
    int VOLATILE = Modifier.VOLATILE.getMask();
    int STRICTFP = Modifier.STRICTFP.getMask();
    int DEFAULT = Modifier.DEFAULT.getMask();

    int getModifiers();

    void setModifiers(int modifiers);

    boolean isPublic();

    void setPublic(boolean isPublic);

    boolean isProtected();

    void setProtected(boolean isProtected);

    boolean isPrivate();

    void setPrivate(boolean isPrivate);

    boolean isAbstract();

    void setAbstract(boolean isAbstract);

    boolean isStatic();

    void setStatic(boolean isStatic);

    boolean isFinal();

    void setFinal(boolean isFinal);

    boolean isSynchronized();

    void setSynchronized(boolean isSynchronized);

    boolean isNative();

    void setNative(boolean isNative);

    boolean isTransient();

    void setTransient(boolean isTransient);

    boolean isVolatile();

    void setVolatile(boolean isVolatile);

    boolean isStrictfp();

    void setStrictfp(boolean isStrictfp);

    boolean isPackagePrivate();

    void setDefault(boolean isDefault);

    boolean isDefault();
}
