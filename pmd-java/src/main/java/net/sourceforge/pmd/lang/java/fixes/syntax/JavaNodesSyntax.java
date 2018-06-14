/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.fixes.syntax;

import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.COLON;
import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.COMMA;
import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.LAMBDA;
import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.LPAREN;
import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.RPAREN;
import static net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.JavaTokenSE.SEMICOLON;

import net.sourceforge.pmd.lang.java.ast.ASTBlock;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTResultType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.fixes.meta.JavaNodesMetaInfo;
import net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.ArraySE;
import net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.ImageSE;
import net.sourceforge.pmd.lang.java.fixes.syntax.structure.element.ModifiersSE;
import net.sourceforge.pmd.lang.syntax.AbstractNodesSyntax;

public class JavaNodesSyntax extends AbstractNodesSyntax<AbstractJavaNode> {
    private static JavaNodesSyntax instance;

    private JavaNodesSyntax(final JavaNodesMetaInfo nodesMetaInfo) {
        super(nodesMetaInfo);
    }

    private static JavaNodesSyntax newInstance(final JavaNodesMetaInfo nodesMetaInfo) {
        final JavaNodesSyntax sjNodesStructure = new JavaNodesSyntax(nodesMetaInfo);
        sjNodesStructure.defineAll();
        return sjNodesStructure;
    }

    public static JavaNodesSyntax getInstance() {
        if (instance == null) {
            instance = newInstance(new JavaNodesMetaInfo());
        }
        return instance;
    }

    private ModifiersSE modifiers() {
        return ModifiersSE.newInstance();
    }

    private ImageSE image() {
        return ImageSE.newInstance();
    }

    private ArraySE array() {
        return ArraySE.newInstance();
    }

    private void defineAll() {
        define(ASTClassOrInterfaceBodyDeclaration.class, modifiers(), child(ASTMethodDeclaration.class));
        define(ASTMethodDeclaration.class,
            child(ASTResultType.class), child(ASTMethodDeclarator.class), COLON,
            child(ASTLambdaExpression.class), SEMICOLON
        );
        define(ASTFormalParameters.class,
            LPAREN,
            zeroOrOne(child(ASTFormalParameter.class), zeroOrMore(COMMA, child(ASTFormalParameter.class))),
            RPAREN
        );
        define(ASTLambdaExpression.class,
            or(
                child(ASTVariableDeclaratorId.class),
                child(ASTFormalParameters.class),
                seq(LPAREN,
                    child(ASTVariableDeclaratorId.class), zeroOrMore(COMMA, child(ASTVariableDeclaratorId.class)),
                    RPAREN
                )
            ), LAMBDA, or(child(ASTExpression.class), child(ASTBlock.class))
        );
        define(ASTVariableDeclaratorId.class, image(), array());
    }
}
