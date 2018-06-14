/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd;

import java.io.Reader;
import java.io.StringReader;

import net.sourceforge.pmd.lang.ast.JavaCharStream;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceBodyDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import net.sourceforge.pmd.lang.java.ast.ASTLambdaExpression;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTResultType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AccessNode;
import net.sourceforge.pmd.lang.java.ast.JavaParser;
import net.sourceforge.pmd.lang.java.fixes.syntax.JavaNodesSyntax;
import net.sourceforge.pmd.lang.syntax.NodeSyntax;

public class Main {
    private static final String CLASS_OR_INTERFACE_BODY_DECLARATION = "static public abstract\n"
        + "    void\n"
        + "        sampleMethod(/* We even remove associated comments! */ int i,\n"
        + "        boolean b,                       float f);";

    private static final String LAMBDA = "(a,Sample.this,c) -> { ; }";

    private Main() {}

    public static void main(String[] args) {
        testClassOrInterfaceBodyDeclaration();
//        testLambda();
    }

    private static void testClassOrInterfaceBodyDeclaration() {
        final String s = "/**\n"
            + " * BSD-style license; for more info see http://pmd.sourceforge.net/license.html\n"
            + " */\n"
            + "\n"
            + "package samples;\n"
            + "\n"
            + "public abstract class JavaParserSample {\n"
            + "    public abstract void sampleMethod(int i, boolean b,                       float f);\n"
            + "}";
        final ASTCompilationUnit cu = new JavaParser(new JavaCharStream(new StringReader(s))).CompilationUnit();
        final ASTClassOrInterfaceBodyDeclaration rootNode = cu.getFirstDescendantOfType(ASTClassOrInterfaceBodyDeclaration.class);
        // final ASTClassOrInterfaceBodyDeclaration rootNode = new CustomJavaParser<ASTClassOrInterfaceBodyDeclaration>(CLASS_OR_INTERFACE_BODY_DECLARATION).getNode();
        // Print the node's structure to easily search the indexes of the require nodes.
        rootNode.dump("> ");
        printSplitter();
        rootNode.print();
        printSplitter();

        final NodeSyntax<ASTFormalParameters> formalParametersStructure = JavaNodesSyntax.getInstance().getNodeSyntax(ASTFormalParameters.class);
        final ASTMethodDeclaration methodDeclaration = (ASTMethodDeclaration) rootNode.jjtGetChild(0);
        final ASTResultType resultType = (ASTResultType) methodDeclaration.jjtGetChild(0);
        final ASTMethodDeclarator methodDeclarator = (ASTMethodDeclarator) methodDeclaration.jjtGetChild(1);

        final ASTFormalParameters formalParameters = (ASTFormalParameters) methodDeclarator.jjtGetChild(0);
        final Node firstFormalParameter = formalParameters.jjtGetChild(0);
        final Node secondFormalParameter = formalParameters.jjtGetChild(1);
        final Node thirdFormalParameter = formalParameters.jjtGetChild(2);

        // No parameters
        firstFormalParameter.remove();
        secondFormalParameter.remove();
        thirdFormalParameter.remove();
        rootNode.print();
        printSplitter();

        // One parameter
        formalParameters.addChild(0, thirdFormalParameter); // Insert it as the first child
        rootNode.print();
        printSplitter();

        // Two parameters
        formalParameters.addChild(1, firstFormalParameter); // Insert it as the second child
        rootNode.print();
        printSplitter();

        // Remove the previous two formal parameters and add the one missing before, sync at the end and print.
        firstFormalParameter.remove();
        thirdFormalParameter.remove();
        formalParameters.addChild(secondFormalParameter);
        rootNode.print();
        printSplitter();

        // Remove ResultType and print (should throw that node is not well-formed)
        resultType.remove();
        try {
            rootNode.print();
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
        printSplitter();

        // Re-Insert ResultType and print (now it should say everything is OK)
        methodDeclaration.addChild(0, resultType);
        rootNode.print();
        printSplitter();

        // Replace the Second Formal Parameter with the Third Formal Parameter
        formalParameters.setChild(0, thirdFormalParameter);
        rootNode.print();
        printSplitter();

        // Create a new FormalParameters containing the First & Second Formal Parameters :D
        final ASTFormalParameters newFormalParameters = formalParametersStructure.newInstance(secondFormalParameter, firstFormalParameter);
        if (newFormalParameters == null) {
            System.out.println("New formal parameters is null. Check this as it was not expected...");
        } else {
            newFormalParameters.print();
        }
        printSplitter();

        // ==================== Test Modifiers ==================== //
        printModifiers(methodDeclaration);
        printSplitter();

        /*
         * Remove one existing modifier & other that does not exists,
         *  just to check it does not cause any problems to remove sth that is not present already.
         */
        methodDeclaration.setAbstract(false);
        methodDeclaration.setFinal(false);
        rootNode.print();
        printSplitter();

        // Changes in modifiers that test the order in which they are inserted.
        // Replace public with private
        methodDeclaration.setPublic(false);
        methodDeclaration.setPrivate(true);
        methodDeclaration.setVolatile(true);
        rootNode.print();
        printSplitter();

        // No modifiers
        methodDeclaration.setPrivate(false);
        methodDeclaration.setStatic(false);
        methodDeclaration.setVolatile(false);
        rootNode.print();
        printSplitter();

        methodDeclaration.setStatic(true); // This should be the first modifier
        methodDeclaration.setPrivate(true); // This should come before `static`
        methodDeclaration.setFinal(true); // This should come after `static`
        rootNode.print();
        printSplitter();
        printModifiers(methodDeclaration);
        printSplitter();
    }

    private static void testLambda() {
        final ASTLambdaExpression lambdaExpression = new CustomJavaParser<ASTLambdaExpression>(LAMBDA).getNode();
        final ASTVariableDeclaratorId firstVariableDeclaratorId = (ASTVariableDeclaratorId) lambdaExpression.jjtGetChild(0);
        final ASTVariableDeclaratorId secondVariableDeclaratorId = (ASTVariableDeclaratorId) lambdaExpression.jjtGetChild(1);
        final ASTVariableDeclaratorId thirdVariableDeclaratorId = (ASTVariableDeclaratorId) lambdaExpression.jjtGetChild(2);

        // Remove all Variable Declarator Ids
        firstVariableDeclaratorId.remove();
        secondVariableDeclaratorId.remove();
        thirdVariableDeclaratorId.remove();
        try {
            lambdaExpression.print();
        } catch (IllegalStateException e) {
            // To have no lambda parameters, we need to assign an empty FormalParameters according to node's syntax.
            // Because of that, this exception is expected, and that's why we handle it.
            System.out.println(e.getMessage());
        }
        printSplitter();

        // Insert the secondVariableDeclaratorId.
        lambdaExpression.addChild(0, secondVariableDeclaratorId);
        lambdaExpression.print();
        printSplitter();

        // Insert the thirdVariableDeclaratorId first, and check it adds a comma between variable declarator ids.
        lambdaExpression.addChild(0, thirdVariableDeclaratorId);
        lambdaExpression.print();
        printSplitter();

        // Insert the firstVariableDeclaratorId as the third child and check that it still chooses the parenthesis option.
        lambdaExpression.addChild(2, firstVariableDeclaratorId);
        lambdaExpression.print();
        printSplitter();

        // ==================== Variable Declarator Id ==================== //
        // Change `a` image to `QWERTY.this`
        firstVariableDeclaratorId.syncRequired();
        firstVariableDeclaratorId.setImage("QWERTY.this");
        lambdaExpression.print();
        printSplitter();

        // Change `QWERTY.this` image to `this`
        firstVariableDeclaratorId.syncRequired();
        firstVariableDeclaratorId.setImage("this");
        lambdaExpression.print();
        printSplitter();

        // Change `this` image to `A`
        firstVariableDeclaratorId.syncRequired();
        firstVariableDeclaratorId.setImage("A");
        lambdaExpression.print();
        printSplitter();

        // Increment array depth by 2 (should be 2)
        firstVariableDeclaratorId.syncRequired();
        firstVariableDeclaratorId.incrementArrayDepth();
        firstVariableDeclaratorId.incrementArrayDepth();
        lambdaExpression.print();
        printSplitter();

        // Decrement array depth by 1 (should be 1)
        firstVariableDeclaratorId.syncRequired();
        firstVariableDeclaratorId.decrementArrayDepth();
        lambdaExpression.print();
        printSplitter();

        // Decrement, again, array depth by 1 (should be 0)
        firstVariableDeclaratorId.syncRequired();
        firstVariableDeclaratorId.decrementArrayDepth();
        lambdaExpression.print();
        printSplitter();

        // Increment array depth by 1 (should be 1)
        firstVariableDeclaratorId.syncRequired();
        firstVariableDeclaratorId.incrementArrayDepth();
        lambdaExpression.print();
        printSplitter();

        // ------------ Creation :D ------------ //
        final NodeSyntax<ASTVariableDeclaratorId> variableDeclaratorIdNodeSyntax = JavaNodesSyntax.getInstance().getNodeSyntax(ASTVariableDeclaratorId.class);
        final ASTVariableDeclaratorId fourthVariableDeclaratorId = variableDeclaratorIdNodeSyntax.newInstance();
        fourthVariableDeclaratorId.setImage("myNewLambdaArray");
        fourthVariableDeclaratorId.incrementArrayDepth();
        lambdaExpression.addChild(3, fourthVariableDeclaratorId);
        lambdaExpression.print();
        printSplitter();
    }

    private static void testModifiers(final Node rootNode) {
        final ASTMethodDeclaration methodDeclaration = (ASTMethodDeclaration) rootNode.jjtGetChild(0);
    }

    private static void printModifiers(final AccessNode accessNode) {
        System.out.println("is public: " + accessNode.isPublic());
        System.out.println("is private: " + accessNode.isPrivate());
        System.out.println("is static: " + accessNode.isStatic());
        System.out.println("is abstract: " + accessNode.isAbstract());
        System.out.println("is final: " + accessNode.isFinal());
    }

    private static void printSplitter() {
        System.out.println("-------------------------------------------------");
    }

    // TODO: this class should be available for all rules
    private static class CustomJavaParser<T> extends JavaParser {

        CustomJavaParser(final String stream) {
            super(new JavaCharStream(new StringReader(stream)));
        }

        CustomJavaParser(final Reader reader) {
            super(new JavaCharStream(reader));
        }

        public T getNode() {
            return (T) jjtree.popNode();
        }
    }
}
