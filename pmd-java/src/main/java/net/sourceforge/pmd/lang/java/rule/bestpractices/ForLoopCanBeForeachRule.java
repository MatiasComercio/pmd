/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.autofix.NodeFixer;
import net.sourceforge.pmd.autofix.RuleViolationAutoFixer;
import net.sourceforge.pmd.lang.ast.CharStream;
import net.sourceforge.pmd.lang.ast.JavaCharStream;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTForInit;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForUpdate;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTRelationalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.ASTVariableInitializer;
import net.sourceforge.pmd.lang.java.ast.JavaParser;
import net.sourceforge.pmd.lang.java.ast.JavaParserTokenManager;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration;
import net.sourceforge.pmd.lang.java.typeresolution.TypeHelper;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;

/**
 * @author Clément Fournier
 * @since 6.0.0
 */
public class ForLoopCanBeForeachRule extends AbstractJavaRule {

    public ForLoopCanBeForeachRule() {
        addRuleChainVisit(ASTForStatement.class);
    }
    
    @Override
    public Object visit(ASTForStatement node, Object data) {

        final ASTForInit init = node.getFirstChildOfType(ASTForInit.class);
        final ASTForUpdate update = node.getFirstChildOfType(ASTForUpdate.class);
        final ASTExpression guardCondition = node.getFirstChildOfType(ASTExpression.class);

        if (init == null && update == null || guardCondition == null) {
            return data;
        }

        Entry<VariableNameDeclaration, List<NameOccurrence>> indexDecl = getIndexVarDeclaration(init, update);

        if (indexDecl == null) {
            return data;
        }


        List<NameOccurrence> occurrences = indexDecl.getValue();
        VariableNameDeclaration index = indexDecl.getKey();

        if (TypeHelper.isA(index, Iterator.class)) {
            Entry<VariableNameDeclaration, List<NameOccurrence>> iterableInfo = getIterableDeclOfIteratorLoop(index, node.getScope());

            if (iterableInfo != null && isReplaceableIteratorLoop(indexDecl, guardCondition, iterableInfo, node)) {
                addViolation(data, node);
            }
            return data;
        }


        if (occurrences == null || !"int".equals(index.getTypeImage()) || !indexStartsAtZero(index)) {
            return data;
        }


        String itName = index.getName();
        String iterableName = getIterableNameOrNullToAbort(guardCondition, itName);


        if (!isForUpdateSimpleEnough(update, itName) || iterableName == null) {
            return data;
        }

        Entry<VariableNameDeclaration, List<NameOccurrence>> iterableInfo = findDeclaration(iterableName, node.getScope());
        VariableNameDeclaration iterableDeclaration = iterableInfo == null ? null : iterableInfo.getKey();

        if (iterableDeclaration == null) {
            return data;
        }

        if (iterableDeclaration.isArray() && isReplaceableArrayLoop(node, occurrences, iterableDeclaration)) {
            addViolation(data, node);
        } else if (iterableDeclaration.getTypeImage() != null && iterableDeclaration.getTypeImage()
                                                                                    .matches("List|ArrayList|LinkedList")
            && isReplaceableListLoop(node, occurrences, iterableDeclaration)) {
            addViolation(data, node);
            // `new ListLoopFix()` can be just one static instance (it does not use any inner state)
            // addViolation(data, node, new ListLoopFix()); // TODO: uncomment
        }

        return data;
    }

    private static class ListLoopFix implements RuleViolationAutoFixer {
        private static String stream(final String varType, final String varName, final String collectionName) {
            return String.format("for (%s %s : %s) { ; }", varType, varName, collectionName);
        }

        @Override
        public void apply(final Node node, final NodeFixer nodeFixer) {
            final ASTForStatement forStatement = (ASTForStatement) node;

            final String varType = ""; // TODO
            final String varName = ""; // TODO
            final String collectionName = ""; // TODO

            final String stream = stream(varType, varName, collectionName);
            final CustomJavaParser<ASTForStatement> javaParser = new CustomJavaParser<>(stream);
            final ASTForStatement forEachStatement = javaParser.getNode();
            forStatement.setChild(forEachStatement.jjtGetChild(0), 0); // replace ForInit with LocalVariableDeclaration
            forStatement.setChild(forEachStatement.jjtGetChild(1), 1); // replace Expression with new Expression
            forStatement.removeChild(2); // remove ForUpdate
            // Leave the Statement as it will be the same for both for

            // Build the new local variable declaration
            // TODO: have to get the variable type, which is the type of the list
            // TODO: have to write a variable name if it does not exists; if it exists in the statement,
            //  we should use that name
            // TODO: with all these, we should create a LocalVariableDeclaration

            // TODO: then, create an Expression with a PrimaryExpression, inside a PrimaryPrefix, and then a name.

            // TODO: it would be nice to build the string directly and to compile that into what we need, as
            //  I've proposed before in a Trello card. Let's do that instead, to show that it is much easier in this case.
        }
    }

    private static class CustomJavaParser<T> extends JavaParser {

        CustomJavaParser(final String stream) {
            super(new JavaCharStream(new StringReader(stream)));
        }

        public T getNode() {
            return (T) jjtree.popNode();
        }
    }

    /* Finds the declaration of the index variable and its occurrences */
    private Entry<VariableNameDeclaration, List<NameOccurrence>> getIndexVarDeclaration(ASTForInit init, ASTForUpdate update) {
        if (init == null) {
            return guessIndexVarFromUpdate(update);
        }

        Map<VariableNameDeclaration, List<NameOccurrence>> decls = init.getScope().getDeclarations(VariableNameDeclaration.class);
        Entry<VariableNameDeclaration, List<NameOccurrence>> indexVarAndOccurrences = null;

        for (Entry<VariableNameDeclaration, List<NameOccurrence>> e : decls.entrySet()) {

            ASTForInit declInit = e.getKey().getNode().getFirstParentOfType(ASTForInit.class);
            if (declInit == init) {
                indexVarAndOccurrences = e;
                break;
            }
        }

        return indexVarAndOccurrences;

    }


    /** Does a best guess to find the index variable, gives up if the update has several statements */
    private Entry<VariableNameDeclaration, List<NameOccurrence>> guessIndexVarFromUpdate(ASTForUpdate update) {

        Node name = null;
        try {
            List<Node> match = update.findChildNodesWithXPath(getSimpleForUpdateXpath(null));
            if (!match.isEmpty()) {
                name = match.get(0);
            }
        } catch (JaxenException je) {
            throw new RuntimeException(je);
        }

        if (name == null || name.getImage() == null) {
            return null;
        }

        return findDeclaration(name.getImage(), update.getScope().getParent());
    }


    /**
     * @return true if there's only one update statement of the form i++ or ++i.
     */
    private boolean isForUpdateSimpleEnough(ASTForUpdate update, String itName) {
        return update != null && update.hasDescendantMatchingXPath(getSimpleForUpdateXpath(itName));
    }


    private String getSimpleForUpdateXpath(String itName) {
        return "./StatementExpressionList[count(*)=1]"
            + "/StatementExpression"
            + "/*[self::PostfixExpression and @Image='++' or self::PreIncrementExpression]"
            + "/PrimaryExpression"
            + "/PrimaryPrefix"
            + "/Name"
            + (itName == null ? "" : ("[@Image='" + itName + "']"));
    }


    /* We only report loops with int initializers starting at zero. */
    private boolean indexStartsAtZero(VariableNameDeclaration index) {
        ASTVariableDeclaratorId name = (ASTVariableDeclaratorId) index.getNode();
        ASTVariableDeclarator declarator = name.getFirstParentOfType(ASTVariableDeclarator.class);

        if (declarator == null) {
            return false;
        }

        try {
            List<Node> zeroLiteral = declarator.findChildNodesWithXPath(
                "./VariableInitializer/Expression/PrimaryExpression/PrimaryPrefix/Literal[@Image='0' and "
                    + "@StringLiteral='false']");
            if (!zeroLiteral.isEmpty()) {
                return true;
            }
        } catch (JaxenException je) {
            throw new RuntimeException(je);
        }

        return false;

    }


    /**
     * Gets the name of the iterable array or list.
     *
     * @param itName The name of the iterator variable
     *
     * @return The name, or null if it couldn't be found or the guard condition is not safe to refactor (then abort)
     */
    private String getIterableNameOrNullToAbort(ASTExpression guardCondition, String itName) {


        if (guardCondition.jjtGetNumChildren() > 0
            && guardCondition.jjtGetChild(0) instanceof ASTRelationalExpression) {

            ASTRelationalExpression relationalExpression = (ASTRelationalExpression) guardCondition.jjtGetChild(0);

            if (relationalExpression.hasImageEqualTo("<") || relationalExpression.hasImageEqualTo("<=")) {

                try {
                    List<Node> left = guardCondition.findChildNodesWithXPath(
                        "./RelationalExpression/PrimaryExpression/PrimaryPrefix/Name[@Image='" + itName + "']");

                    List<Node> right = guardCondition.findChildNodesWithXPath(
                        "./RelationalExpression[@Image='<']/PrimaryExpression/PrimaryPrefix"
                            + "/Name[matches(@Image,'\\w+\\.(size|length)')]"
                            + "|"
                            + "./RelationalExpression[@Image='<=']/AdditiveExpression[count(*)=2 and "
                            + "@Image='-' and PrimaryExpression/PrimaryPrefix/Literal[@Image='1']]"
                            + "/PrimaryExpression/PrimaryPrefix/Name[matches(@Image,'\\w+\\.(size|length)')]");

                    if (left.isEmpty()) {
                        return null;
                    } else if (!right.isEmpty()) {
                        return right.get(0).getImage().split("\\.")[0];
                    } else {
                        return null;
                    }

                } catch (JaxenException je) {
                    throw new RuntimeException(je);
                }
            }
        }
        return null;
    }


    private Entry<VariableNameDeclaration, List<NameOccurrence>> getIterableDeclOfIteratorLoop(VariableNameDeclaration indexDecl, Scope scope) {
        Node initializer = indexDecl.getNode().getFirstParentOfType(ASTVariableDeclarator.class)
                                    .getFirstChildOfType(ASTVariableInitializer.class);

        if (initializer == null) {
            return null;
        }

        ASTName nameNode = initializer.getFirstDescendantOfType(ASTName.class);
        if (nameNode == null) {
            // TODO : This can happen if we are calling a local / statically imported method that returns the iterable - currently unhandled
            return null;
        }
        
        String name = nameNode.getImage();
        int dotIndex = name.indexOf('.');

        if (dotIndex > 0) {
            name = name.substring(0, dotIndex);
        }

        return findDeclaration(name, scope);
    }


    private boolean isReplaceableArrayLoop(ASTForStatement stmt, List<NameOccurrence> occurrences,
                                           VariableNameDeclaration arrayDeclaration) {
        String arrayName = arrayDeclaration.getName();


        for (NameOccurrence occ : occurrences) {

            if (occ.getLocation().getFirstParentOfType(ASTForUpdate.class) == null
                && occ.getLocation().getFirstParentOfType(ASTExpression.class)
                != stmt.getFirstChildOfType(ASTExpression.class)
                && !occurenceIsArrayAccess(occ, arrayName)) {
                return false;
            }
        }
        return true;
    }


    private boolean occurenceIsArrayAccess(NameOccurrence occ, String arrayName) {
        if (occ.getLocation() instanceof ASTName) {
            ASTPrimarySuffix suffix = occ.getLocation().getFirstParentOfType(ASTPrimarySuffix.class);

            if (suffix == null || !suffix.isArrayDereference()) {
                return false;
            }

            return suffix.hasDescendantMatchingXPath("./Expression/PrimaryExpression[count(*)"
                                                         + "=1]/PrimaryPrefix/Name[@Image='" + occ.getImage() + "']")
                && suffix.hasDescendantMatchingXPath("../PrimaryPrefix/Name[@Image='" + arrayName + "']")
                && !suffix.hasDescendantMatchingXPath("../../AssignmentOperator");
        }
        return false;
    }


    private boolean isReplaceableListLoop(ASTForStatement stmt, List<NameOccurrence> occurrences,
                                          VariableNameDeclaration listDeclaration) {

        String listName = listDeclaration.getName();


        for (NameOccurrence occ : occurrences) {

            if (occ.getLocation().getFirstParentOfType(ASTForUpdate.class) == null
                && occ.getLocation().getFirstParentOfType(ASTExpression.class)
                != stmt.getFirstChildOfType(ASTExpression.class)
                && !occurenceIsListGet(occ, listName)) {
                return false;
            }
        }

        return true;
    }


    /** @return true if this occurence is as an argument to List.get on the correct list */
    private boolean occurenceIsListGet(NameOccurrence occ, String listName) {
        if (occ.getLocation() instanceof ASTName) {
            ASTPrimarySuffix suffix = occ.getLocation().getFirstParentOfType(ASTPrimarySuffix.class);

            if (suffix == null) {
                return false;
            }

            Node prefix = suffix.jjtGetParent().jjtGetChild(0);

            if (!(prefix instanceof ASTPrimaryPrefix) && prefix.jjtGetNumChildren() != 1
                && !(prefix.jjtGetChild(0) instanceof ASTName)) {
                return false;
            }

            String callImage = prefix.jjtGetChild(0).getImage();

            return (listName + ".get").equals(callImage);

        }
        return false;
    }


    private Entry<VariableNameDeclaration, List<NameOccurrence>> findDeclaration(String varName, Scope innermost) {
        Scope currentScope = innermost;

        while (currentScope != null) {
            for (Entry<VariableNameDeclaration, List<NameOccurrence>> e : currentScope.getDeclarations(VariableNameDeclaration.class).entrySet()) {
                if (e.getKey().getName().equals(varName)) {
                    return e;
                }
            }
            currentScope = currentScope.getParent();
        }

        return null;
    }


    private boolean isReplaceableIteratorLoop(Entry<VariableNameDeclaration, List<NameOccurrence>> indexInfo,
                                              ASTExpression guardCondition,
                                              Entry<VariableNameDeclaration, List<NameOccurrence>> iterableInfo,
                                              ASTForStatement stmt) {

        if (isIterableModifiedInsideLoop(iterableInfo, stmt)) {
            return false;
        }


        String indexName = indexInfo.getKey().getName();

        if (indexName == null) {
            return false;
        }

        if (!guardCondition.hasDescendantMatchingXPath(
            "./PrimaryExpression/PrimaryPrefix/Name[@Image='" + indexName + ".hasNext']")) {
            return false;
        }

        List<NameOccurrence> occurrences = indexInfo.getValue();

        if (occurrences.size() > 2) {
            return false;
        }

        for (NameOccurrence occ : indexInfo.getValue()) {
            String image = occ.getLocation().getImage();

            if (occ.getLocation() instanceof ASTName
                && ((indexName + ".hasNext").equals(image) || (indexName + ".next").equals(image))) {
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean isIterableModifiedInsideLoop(Entry<VariableNameDeclaration, List<NameOccurrence>> iterableInfo,
                                                 ASTForStatement stmt) {

        String iterableName = iterableInfo.getKey().getName();
        for (NameOccurrence occ : iterableInfo.getValue()) {
            ASTForStatement forParent = occ.getLocation().getFirstParentOfType(ASTForStatement.class);
            if (forParent == stmt) {
                String image = occ.getLocation().getImage();
                if (image.startsWith(iterableName + ".remove")) {
                    return true;
                }
            }
        }

        return false;
    }


}
