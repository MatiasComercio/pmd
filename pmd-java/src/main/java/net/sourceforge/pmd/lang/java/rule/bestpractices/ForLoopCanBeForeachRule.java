/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices;

import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTCLASSORINTERFACETYPE;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTEXPRESSION;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTLOCALVARIABLEDECLARATION;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTNAME;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTPRIMARYEXPRESSION;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTPRIMARYPREFIX;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTREFERENCETYPE;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTTYPE;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTVARIABLEDECLARATOR;
import static net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants.JJTVARIABLEDECLARATORID;

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.type.ReferenceType;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.autofix.NodeFixer;
import net.sourceforge.pmd.autofix.RuleViolationAutoFixer;
import net.sourceforge.pmd.lang.ast.JavaCharStream;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTForInit;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForUpdate;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTReferenceType;
import net.sourceforge.pmd.lang.java.ast.ASTRelationalExpression;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTTypeArgument;
import net.sourceforge.pmd.lang.java.ast.ASTTypeArguments;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.ASTVariableInitializer;
import net.sourceforge.pmd.lang.java.ast.JavaParser;
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
            // addViolation(data, node, new ListLoopFix(iterableDeclaration, occurrences)); // TODO: This is the way to report the fixer to use for the detected violation
        }

        return data;
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
    private static boolean occurenceIsListGet(NameOccurrence occ, String listName) {
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

    // =====================================================================================
    private static class ListLoopFix implements RuleViolationAutoFixer {
        private final VariableNameDeclaration iterableDeclaration;
        private final List<NameOccurrence> indexOccurrences;
        private final ListGetOccurrences listGetOccurrences;

        private ListLoopFix(final VariableNameDeclaration pIterableDeclaration,
                            final List<NameOccurrence> pIndexOccurrences) {
            this.iterableDeclaration = pIterableDeclaration;
            this.indexOccurrences = pIndexOccurrences;
            this.listGetOccurrences = getListGetIndexOccurrences(iterableDeclaration, indexOccurrences);
        }

        @Override
        // TODO: update SCOPE correctly; not doing it here because it should be done `transparently` for the user
        public void apply(final Node forStatement, final NodeFixer nodeFixer) {
            // Update the first `for` child node (i.e., the ForInit node)
            final ASTLocalVariableDeclaration localVariableDeclaration = buildLocalVariableDeclaration(iterableDeclaration);
            forStatement.setChild(localVariableDeclaration, 0);
            // TODO: think: Perhaps, other way to do it can be:
            //  forStatement.getFirstChildOfType(ASTForInit.class).replaceWith(localVariableDeclaration);
            // This will replace the self node with the given node at the self node index in the parent

            // Update the expression child of the for loop to match the for each expression
            final ASTExpression astExpression = ASTExpression.class.cast(forStatement.jjtGetChild(1));
            astExpression.setChild(buildPrimaryExpression(iterableDeclaration), 0);
            // Remove the third child of the `for` node (i.e., the ForUpdate node)
            forStatement.removeChild(2);
            // TODO: IMPORTANT: This can also be performed in the following way:
            //  forStatement.getFirstChildOfType(ASTForUpdate.class).remove();

            final ASTExpression newForeachVariableExpression = buildNewForEachVariableExpression(localVariableDeclaration);
            replaceListAccessWithForEachVariable(listGetOccurrences, newForeachVariableExpression);
        }

        private ASTExpression buildNewForEachVariableExpression(final ASTLocalVariableDeclaration localVariableDeclaration) {
            final VariableNameDeclaration variableNameDeclaration =
                localVariableDeclaration.getFirstDescendantOfType(ASTVariableDeclaratorId.class).getNameDeclaration();
            final ASTExpression expression = new ASTExpression(JJTEXPRESSION);
            expression.setChild(buildPrimaryExpression(variableNameDeclaration), 0);
            return expression;
        }

        private void replaceListAccessWithForEachVariable(final ListGetOccurrences pListGetOccurrences,
                                                          final ASTExpression newForeachVariableExpression) {
            for (final ASTExpression expression : pListGetOccurrences.assignmentExpressions) {
                // It has to be removed in favor of the foreach variable
                // Note that if this is an assignment, then the `for` statement is enforced to have a block to compile
                expression.getFirstParentOfType(ASTBlockStatement.class).remove();
            }

            for (final ASTExpression expression : pListGetOccurrences.usageExpressions) {
                // It has to be replaced with the foreach variable
                expression.jjtGetParent().setChild(newForeachVariableExpression/*.clone() TODO */, expression.jjtGetChildIndex());
                // expression.replaceWith(newForeachVariableExpression); // TODO: another usage of the `replaceWith` method
            }
        }

        private static class ListGetOccurrences {
            private final List<ASTExpression> assignmentExpressions;
            private final List<ASTExpression> usageExpressions;

            private ListGetOccurrences(final List<ASTExpression> pAssignmentExpressions,
                                       final List<ASTExpression> pUsageExpressions) {
                this.assignmentExpressions = pAssignmentExpressions;
                this.usageExpressions = pUsageExpressions;
            }
        }

        private ListGetOccurrences getListGetIndexOccurrences(final VariableNameDeclaration listDeclaration,
                                                              final List<NameOccurrence> pIndexOccurrences) {
            final String listName = listDeclaration.getName();

            final List<ASTExpression> assignmentExpressions = new LinkedList<>();
            final List<ASTExpression> usageExpressions = new LinkedList<>();

            for (NameOccurrence indexOccurrence : pIndexOccurrences) {
                if (!occurenceIsListGet(indexOccurrence, listName)) {
                    continue;
                }
                final ASTExpression expression = indexOccurrence.getLocation().getFirstParentOfType(ASTExpression.class);
                if (expression.jjtGetParent() instanceof ASTVariableInitializer) {
                    assignmentExpressions.add(expression);
                } else {
                    usageExpressions.add(expression);
                }
            }

            return new ListGetOccurrences(assignmentExpressions, usageExpressions);
        }

        private ASTPrimaryExpression buildPrimaryExpression(final VariableNameDeclaration pIterableDeclaration) {
            final ASTName iterableName = new ASTName(JJTNAME);
            iterableName.setImage(pIterableDeclaration.getImage());
            iterableName.setType(pIterableDeclaration.getType());
            iterableName.setNameDeclaration(pIterableDeclaration);
            final ASTPrimaryPrefix primaryPrefix = new ASTPrimaryPrefix(JJTPRIMARYPREFIX);
            primaryPrefix.setChild(iterableName, 0);
            final ASTPrimaryExpression primaryExpression = new ASTPrimaryExpression(JJTPRIMARYEXPRESSION);
            primaryExpression.setChild(primaryPrefix, 0);
            return primaryExpression;
        }

        private ASTLocalVariableDeclaration buildLocalVariableDeclaration(final VariableNameDeclaration pIterableDeclaration) {
            final ASTLocalVariableDeclaration localVariableDeclaration =
                new ASTLocalVariableDeclaration(JJTLOCALVARIABLEDECLARATION);
            localVariableDeclaration.setChild(buildType(pIterableDeclaration), 0);
            localVariableDeclaration.setChild(buildVariableDeclarator(pIterableDeclaration), 1);
            return localVariableDeclaration;
        }

        private ASTVariableDeclarator buildVariableDeclarator(final VariableNameDeclaration pIterableDeclaration) {
            final ASTVariableDeclaratorId variableDeclaratorId = buildVariableDeclaratorId(listGetOccurrences, pIterableDeclaration);
            final ASTVariableDeclarator variableDeclarator = new ASTVariableDeclarator(JJTVARIABLEDECLARATOR);
            variableDeclarator.setChild(variableDeclaratorId, 0);
            return variableDeclarator;
        }


        private static final String ORIGINAL_IMAGE = "aListElem";
        /**
         * If there is a statement in the `for` body like `T elem = list.get(i)`, grab the `elem` name;
         * if not, create a variable name such as it does not already exist in the scope.
         */
        private ASTVariableDeclaratorId buildVariableDeclaratorId(final ListGetOccurrences pListGetOccurrences,
                                                                  final VariableNameDeclaration pIterableDeclaration) {
            if (!pListGetOccurrences.assignmentExpressions.isEmpty()) {
                // Just getting the first one that will be the only one assigned; other assignments should be removed,
                // and those variables usages replaced with the new one // TODO
                return pListGetOccurrences.assignmentExpressions.get(0)
                    .getFirstParentOfType(ASTLocalVariableDeclaration.class)
                    .getFirstDescendantOfType(ASTVariableDeclaratorId.class)/*.clone() TODO */;
            }

            // Create a variable name that does not exist in the scope
            final Scope scope = pIterableDeclaration.getScope();

            int i = 1;
            String newImage = ORIGINAL_IMAGE;
            // TODO: find if there is any NameDeclaration of the given class already declared with the given image
            // TODO: idea of implementation: getDeclarations of the given class and iterate all over those declarations
            //  comparing the given image with the image of each iteration.
            //  This should be done not only for current scope but up to the root scope,
            //      so as to ensure we are not screwing it up overriding an already declared variable
            //      and changing its value in the current scope
            //  Note that the `equals` of VariableNameDeclaration is done through the image field
            while (scope.isDeclaredAs(VariableNameDeclaration.class, newImage)) { // TODO: not now, but bare it in mind
                newImage = ORIGINAL_IMAGE + i++;
            }

            // Create the node and the variable declaration with the chosen image
            final ASTVariableDeclaratorId variableDeclaratorId = new ASTVariableDeclaratorId(JJTVARIABLEDECLARATORID);
            variableDeclaratorId.setImage(newImage);
            final VariableNameDeclaration nameDeclaration = new VariableNameDeclaration(variableDeclaratorId);
            variableDeclaratorId.getScope().addDeclaration(nameDeclaration);
            variableDeclaratorId.setNameDeclaration(nameDeclaration);
            return variableDeclaratorId;
        }

        private ASTType buildType(final VariableNameDeclaration pIterableDeclaration) {
            final ASTFormalParameter formalParameter = pIterableDeclaration.getNode().getFirstParentOfType(ASTFormalParameter.class);
            ASTReferenceType listReferenceType =
                formalParameter.getFirstDescendantOfType(ASTReferenceType.class).getFirstChildOfType(ASTReferenceType.class);
            if (listReferenceType == null) { // no generic type for list
                listReferenceType = new ASTReferenceType(JJTREFERENCETYPE);
                final ASTClassOrInterfaceType objectClassOrInterfaceType = new ASTClassOrInterfaceType(JJTCLASSORINTERFACETYPE);
                objectClassOrInterfaceType.setType(Object.class);
                listReferenceType.setChild(objectClassOrInterfaceType, 0); // TODO: this may be `append`? I think it would be nice :smile:
            } else {
                listReferenceType = listReferenceType/*.clone() TODO */; // So as not to detach the old node from the original parent
                // TODO: this clone should be intelligent enough to be able to grab the original string from the file,
                //  but to not remove that string region if this cloned node is removed
                //  TODO: i.e., this will be like a `new` node but with an `original` string reference
            }
            final ASTType listType = new ASTType(JJTTYPE);
            listType.setChild(listReferenceType, 0);
            return listType;
        }
    }





    // =====================================================================================

    // TODO: this is the way of making the change building a string to grab the needed node structure.
    //  It may be used in conjunction with the stringify logic to be implemented later.
    private static class StringListLoopFix implements RuleViolationAutoFixer {
        private final VariableNameDeclaration iterableDeclaration;

        private StringListLoopFix(final VariableNameDeclaration pIterableDeclaration) {
            this.iterableDeclaration = pIterableDeclaration;
        }

        private static String stream(final String varType, final String varName, final String collectionName) {
            return String.format("for (%s %s : %s) { ; }", varType, varName, collectionName);
        }

        @Override
        public void apply(final Node node, final NodeFixer nodeFixer) {
            final ASTForStatement forStatement = (ASTForStatement) node;

            final String varType = getVarType();
            /*
             * TODO: have to write a variable name if it does not exists; if it exists in the statement, we should use that name
             * The name to be created can be: aCollectionName[number] where number should start form 1 and be used only
             * if the aCollectionName var exists, so as to ensure that the variable name is unique.
             * So, for example, if aCollectionName, aCollectionName1, aCollectionName2 exist, then the variable name
             * should be aCollectionName3.
             */
            final String varName = "";
            final String collectionName = ""; // TODO: have to get the collection (list) name

            final String stream = stream(varType, varName, collectionName);
            final CustomJavaParser<ASTForStatement> javaParser = new CustomJavaParser<>(stream);
            final ASTForStatement forEachStatement = javaParser.getNode();
            forStatement.setChild(forEachStatement.jjtGetChild(0), 0); // replace ForInit with LocalVariableDeclaration
            forStatement.setChild(forEachStatement.jjtGetChild(1), 1); // replace Expression with new Expression
            forStatement.removeChild(2); // remove ForUpdate

            /*
             * TODO: update the statement so as to replace ALL the get(i) occurrences with the varName name.
             * If there was an entire line that declared the variable inside the statement, remove that line.
             */
        }

        private String getVarType() {
            // iterableDeclaration is sibling of ASTReferenceType, with common parent `ASTType`
            final ASTType astType = iterableDeclaration.getNode().getFirstParentOfType(ASTType.class);
            final Node listClassOrInterfaceType = astType.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
            // As this is a list, it should have 0 or 1 child of type `TypeArguments` & grandchild `TypeArgument`
            // If 0, list elements are of type `Object`
            // Else, list elements are of the type determined by `TypeArguments`
            if (listClassOrInterfaceType.jjtGetNumChildren() == 0) {
                return "Object";
            }

            // Get the TypeArguments, and then the TypeArgument
            return stringify((ASTTypeArgument) listClassOrInterfaceType.jjtGetChild(0).jjtGetChild(0));
        }

        /*
         * ===================================================
         * -- Idea --
         * In the visitor version, if no change is detected in any descendants of the current node, then
         * the visitor should skip that branch (DO NOT stringify it, its pointless), unless a previous call to
         * stringifying an updated node has been called.
         *
         * ===================================================
         * -- Step 1: Finding Rewrite Events --
         *
         * Let's start with an example case, where we have one node with two children, and we are standing
         * on that node, so we know that it hasn't suffered any changes.
         * We may find if our own children have been modified first, in order to check if we should generate
         * a text operation for them.
         * We find that none of them have been modified, so we don't have to rewrite them.
         * Given this, we can check for advance if we have to continue visiting any of these children.
         * If one node descendants haven't suffered any modifications, then its pointless to visit that branch
         * of the ast.
         * We therefore ask, for each child node, if childNode.hasAnyDescendantsBeenModified.
         * We find that the first one has no descendant modified, but the second one does.
         *
         * Given this, we skip the visitation of the first child, as we know that that AST branch has not been modified.
         * Hence, we continue navigating the branch of the second child.
         * When we visit that child, we find that it has two children.
         * The first one hasn't been modified and doesn't have any descendant modified.
         * The second child has been updated itself, so we start CREATING TEXT OPERATIONS
         * JUST FOR THE UPDATED CHILD OF THE CURRENT NODE (i.e., for this second child only).
         * With this in mind, what we do is to skip the first child (it hasn't been modified & its branch hadn't
         * suffered any modification), and generate a text operation to update the string representation of the
         * second child.
         *
         * Note that all this stuff can be performed in a language-independent way,
         * as we are only finding rewrite events, which should be generated for nodes of all PMD supported languages.
         *
         * ===================================================
         * -- Step 2: Translating Rewrite Events to Text Operations --
         * We shall answer the question `How are text operations generated?`
         * Before starting, bare in mind that THIS IS COMPLETE DIFFERENT STUFF/FLOW TO THE MENTIONED ABOVE.
         *
         * Now, we are in a context where we know that the current node has been modified, and that this change
         * has to be represented as a text operation.
         * As THIS node has changed, we may, for sure, generate a new string for the CURRENT node
         * and its characteristics.
         *
         * Note that as nodes of each PMD supported language have their own characteristics,
         * this flow is language-dependent (just in the case of replace/insert events, as we explain below).
         *
         * Let's suppose that we are dealing with Java nodes, and that the current node is a
         * `ClassOrInterfaceDeclaration`. Then this node may have changed its image (i.e., the class name),
         * any of the modifiers (final, public, abstract, static, etc.), or whatsoever.
         * Depending on the type of modification it has been performed, one action may be taken over the other.
         * - Remove modifications are the easiest to deal with, because they can be treated just with the `Node`
         * interface: get the region represented by that node, and remove it from the original source.
         * In this case only, translation of rewrite events into text operations is language-independent.
         * - On the other hand, both insert and replace operations are a bit more tricky.
         *
         * Let's deal with the replace operation, which may be the most difficult one, so as to cover all the cases
         * at once.
         * For making a replacement, we should consider all the region of the node that has been replaced,
         * and transform the new node in its string representation, so as to insert this new text in the region of the
         * old node's matching text.
         * The tricky part is that, as we should represent the current node as string, this involves stringifying all
         * the descendant nodes of the current node. But, it may happen that the current (new) node, that is replacing
         * the original node, has been created with some parts of original nodes (so, its string representation SHOULD
         * be taken from the original file) and some other parts with new nodes (which don't have a string
         * representation, and must therefore be generated).
         *
         * // --------------------------------------------------------------------------------------------------------
         * // Thinking if it should be a good idea to generate all the token string when creating the nodes
         * // and updating its context (i.e., concatenating the new representation with the original tokens),
         * // or just rewriting the nodes as string as I was saying...
         * // I'll go for the second one, because updating tokens context depending on what the user creates
         * // or does not create may be a little resources-consumer and not as simple to do.
         * // Eclipse does as I've chosen.
         * // --------------------------------------------------------------------------------------------------------
         *
         * Resuming the idea, we should discriminate original nodes from new nodes when getting its string
         * representation.
         * Let's keep using the same example. Recall that we are in a `Java` context and that
         * the current node that has replaced the original node is of type `ClassOrInterfaceDeclaration`.
         * So, we get all the current node characteristics as string, and then go and get its children string
         * representation (actually, the order in which the children strings are grabbed depends on the characteristics
         * of each type of node for each language).
         * Let's suppose that the `ClassOrInterfaceDeclaration` node has 2 children (it doesn't matter if this is not
         * even possible; it's just for an illustrative purpose). How do we do to know if a child is original or new?
         * Well, we should first ask if the children have changed themselves.
         * Let's suppose that the first child hasn't changed.
         * So, we ask it firstChildNode.hasAnyDescendantBeenModified.
         * - If this is false, then all the child's children nodes string representation may be taken from the
         * original file. For this, we take the region of the first child node and grab all the string of that region
         * from the original file (perhaps, we can enqueue a read operation or sth of that sort so as to read all
         * string sections from the file at once).
         * // TODO: have to think how to solve this issue so as not to downgrade performance that much
         * - If this is true, then we shall grab each exclusive parent region so as to get the
         * current node string section from the original file, and then concatenate the string
         * for each of its children. The exclusive parent region may be obtained by making a xor between the parent
         * region and each of the children regions. Given this, the exclusive parent region is indeed an array of
         * regions.
         *   The string for each child may be obtained using the same logic as stated above (i.e., build the
         *   new string for the new nodes and grab the original string for the original nodes).
         *
         * ===================================================
         * In this way, we are enforcing to keep the user string representation as much as possible.
         * With the example below, I'll try to show this behaviour.
         *
         * Other notes & keys:
         * - It would be nice to have a lazy computation of the hasAnyDescendantBeenModified method,
         * so as not to make extra operations when not needed, but save the computation result once the ast is traversed
         * to find the answer to this question.
         * - It would be nice if nodes with custom characteristics (like access nodes in java) can have regions
         * for those characteristics identified, in order to just generate text operations for those regions instead
         * that for the entire node region.
         */

        // xnow primitive version: this may be largely improved as explained above
        private String stringify(final ASTTypeArgument typeArgument) {
            final StringBuilder sb = new StringBuilder();
            if (typeArgument.isNew()) {
                stringifyNew(typeArgument, sb);
            } else {
                // stringifyOriginal(typeArgument, sb); // TODO as explained above
            }
            return sb.toString();
        }

        private void stringifyNew(final ASTClassOrInterfaceType classOrInterfaceType, final StringBuilder sb) {
            sb.append(classOrInterfaceType.getImage());
            stringifyChildren(classOrInterfaceType, sb);
        }

        private void stringifyChildren(final ASTClassOrInterfaceType classOrInterfaceType, final StringBuilder sb) {
            for (int i = 0; i < classOrInterfaceType.jjtGetNumChildren(); i++) {
                final Node childNode = classOrInterfaceType.jjtGetChild(i);
                if (!(childNode instanceof ASTTypeArguments)) { // currently, we are expecting only this kind of child
                    continue;
                }
                stringifyNew((ASTTypeArguments) childNode, sb);
            }
        }

        private void stringifyNew(final ASTTypeArguments typeArguments, final StringBuilder sb) {
            sb.append("<");
            for (int i = 0; i < typeArguments.jjtGetNumChildren(); i++) {
                final Node childNode = typeArguments.jjtGetChild(i);
                if (!(childNode instanceof ASTTypeArgument)) { // currently, we are expecting only this kind of child
                    continue;
                }
                stringifyNew((ASTTypeArgument) childNode, sb);
                if (i < typeArguments.jjtGetNumChildren() - 1) { // if this is not the last child
                    sb.append(",");
                }
            }
            sb.append(">");
        }

        private void stringifyNew(final ASTTypeArgument typeArgument, final StringBuilder sb) {
            // TODO: we should handle the annotation & wildcardBounds cases here; see Java.jjt file
            for (int i = 0; i < typeArgument.jjtGetNumChildren(); i++) {
                final Node childNode = typeArgument.jjtGetChild(i);
                if (!(childNode instanceof ReferenceType)) {
                    continue;
                }
                stringifyNew((ASTReferenceType) childNode, sb);
            }
        }

        private void stringifyNew(final ASTReferenceType referenceType, final StringBuilder sb) {
            // TODO: we should handle the annotation cases here; see Java.jjt file
            for (int i = 0; i < referenceType.jjtGetNumChildren(); i++) {
                final Node childNode = referenceType.jjtGetChild(i);
                if (childNode instanceof ASTPrimitiveType) {
                    stringifyNew((ASTPrimitiveType) childNode, sb);
                } else if (childNode instanceof ASTClassOrInterfaceType) {
                    stringifyNew((ASTClassOrInterfaceType) childNode, sb);
                } else {
                    continue;
                }
                for (int j = 0; j < referenceType.getArrayDepth(); j++) {
                    sb.append("[]");
                }
            }
        }

        private void stringifyNew(final ASTPrimitiveType primitiveType, final StringBuilder sb) {
            sb.append(primitiveType.getType());
        }

        /*
         * Note that first removing the old children of the `ForStatement` (`ForInit` & `Expression`)
         * and then inserting the new children (`LocalVariableDeclaration` & `Expression`)
         * works the same because of the merging of the rewrite events
         */
    }

    // TODO: this class should be available for all rules
    private static class CustomJavaParser<T> extends JavaParser {

        CustomJavaParser(final String stream) {
            super(new JavaCharStream(new StringReader(stream)));
        }

        public T getNode() {
            return (T) jjtree.popNode();
        }
    }

}
