/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.fixes.syntax.structure.element;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.java.ast.JavaParserConstants;
import net.sourceforge.pmd.lang.java.fixes.factory.TokenFactory;
import net.sourceforge.pmd.lang.syntax.structure.element.AbstractTokenSE;

public class JavaTokenSE extends AbstractTokenSE {
    private static final int SPACE_TOKEN_TYPE = 1; // Extracted from parsing & debugging a space

    // Enums cannot extend a class but implement an interface, so let's simulate what we need of them...
    public static final JavaTokenSE LPAREN = new JavaTokenSE(JavaParserConstants.LPAREN);
    public static final JavaTokenSE RPAREN = new JavaTokenSE(JavaParserConstants.RPAREN);
    public static final JavaTokenSE LBRACKET = new JavaTokenSE(JavaParserConstants.LBRACKET);
    public static final JavaTokenSE RBRACKET = new JavaTokenSE(JavaParserConstants.RBRACKET);
    public static final JavaTokenSE COMMA = new JavaTokenSE(JavaParserConstants.COMMA);
    public static final JavaTokenSE SEMICOLON = new JavaTokenSE(JavaParserConstants.SEMICOLON);
    public static final JavaTokenSE COLON = new JavaTokenSE(JavaParserConstants.COLON);
    public static final JavaTokenSE LAMBDA = new JavaTokenSE(JavaParserConstants.LAMBDA);
    public static final JavaTokenSE THIS = new JavaTokenSE(JavaParserConstants.THIS);

    // =========================== Modifier Tokens =========================== //
    public static final JavaTokenSE PUBLIC = new JavaTokenSE(JavaParserConstants.PUBLIC);
    public static final JavaTokenSE STATIC = new JavaTokenSE(JavaParserConstants.STATIC);
    public static final JavaTokenSE PROTECTED = new JavaTokenSE(JavaParserConstants.PROTECTED);
    public static final JavaTokenSE PRIVATE = new JavaTokenSE(JavaParserConstants.PRIVATE);
    public static final JavaTokenSE FINAL = new JavaTokenSE(JavaParserConstants.FINAL);
    public static final JavaTokenSE ABSTRACT = new JavaTokenSE(JavaParserConstants.ABSTRACT);
    public static final JavaTokenSE SYNCHRONIZED = new JavaTokenSE(JavaParserConstants.SYNCHRONIZED);
    public static final JavaTokenSE NATIVE = new JavaTokenSE(JavaParserConstants.NATIVE);
    public static final JavaTokenSE TRANSIENT = new JavaTokenSE(JavaParserConstants.TRANSIENT);
    public static final JavaTokenSE VOLATILE = new JavaTokenSE(JavaParserConstants.VOLATILE);
    public static final JavaTokenSE STRICTFP = new JavaTokenSE(JavaParserConstants.STRICTFP);
    public static final JavaTokenSE DEFAULT = new JavaTokenSE(JavaParserConstants.DEFAULT);

    public static final JavaTokenSE SPACE = new JavaTokenSE(SPACE_TOKEN_TYPE);


    private final int tokenType;

    private JavaTokenSE(final int tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public String getImage() {
        return TokenFactory.INSTANCE.imageFor(tokenType);
    }

    @Override
    public GenericToken newToken() {
        return TokenFactory.INSTANCE.newToken(tokenType);
    }
}
