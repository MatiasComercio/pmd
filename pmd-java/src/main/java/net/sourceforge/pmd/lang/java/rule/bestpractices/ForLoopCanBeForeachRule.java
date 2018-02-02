/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.bestpractices;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.type.ReferenceType;

import org.jaxen.JaxenException;

import net.sourceforge.pmd.autofix.NodeFixer;
import net.sourceforge.pmd.autofix.RuleViolationAutoFixer;
import net.sourceforge.pmd.lang.ast.JavaCharStream;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTForInit;
import net.sourceforge.pmd.lang.java.ast.ASTForStatement;
import net.sourceforge.pmd.lang.java.ast.ASTForUpdate;
import net.sourceforge.pmd.lang.java.ast.ASTName;
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
            // addViolation(data, node, new ListLoopFix(iterableDeclaration)); // TODO: This is the way to report the fixer to use for the detected violation
        }

        return data;
    }

    private static class ListLoopFix implements RuleViolationAutoFixer {
        private final VariableNameDeclaration iterableDeclaration;

        private ListLoopFix(final VariableNameDeclaration pIterableDeclaration) {
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

        // xnow primitive version: this may be largely improved
        /*
         * TODO: we should, for each node, decide if it is new or it has changed
         */
        private String stringify(final ASTTypeArgument typeArgument) {
            final StringBuilder sb = new StringBuilder();
            if (typeArgument.isNew()) {
                stringifyNew(typeArgument, sb);
            } else {
                stringifyOriginal(typeArgument, sb);
            }
            return sb.toString();
        }

        /*
         * xnow: in the visitor version, if no change is detected in any descendants of the current node, then
         * the visitor should skip that branch (DO NOT stringify it, its pointless), unless a previous call to
         * stringifying an updated node has been called.
         *
         * For example, we may have one node with two children.
         * The first one has no descendant modified, but the second one does.
         * Then, we ask if we should rewrite the current node.
         * Any of the two child have any direct changes (i.e, in their children), so, we don't rewrite the current node.
         * Moreover, we skip the visitation of the first child, as we know that that AST branch has not been modified.
         * We therefore continue navigating the branch of the second child.
         * When we visit that child, we find that it has two children.
         * The first one has no descendant modified, but the second one does.
         * Indeed, the second child has been updated itself, so we start TRACKING/CREATING TEXT OPERATIONS
         * JUST FOR THE UPDATED CHILDREN OF THE CURRENT NODE.
         * With this in mind, what we do is to skip the first child (its branch hadn't suffered any modification),
         * and then generate a text operation to update the second child.
         *
         * How is the text operation generated?
         * THIS IS COMPLETE DIFFERENT STUFF TO THE MENTIONED ABOVE.
         * Now, we know that at least the current node has changed, and that this change has to be represented as a text
         * operation.
         * As THIS node has changed, we may, for sure, generate a new string for the CURRENT node characteristics.
         * For example, if the current node were a `ClassOrInterfaceDeclaration` (java specialized node), then
         * this node may have changed its image (i.e., the class name), any of the modifiers
         * (final, public, abstract, static, etc.), or whatsoever.
         * 
         *
         * Other notes & keys:
         * - It would be nice to have a lazy computation of the hasAnyDescendantBeenModified method,
         * so as not to make extra operations when not needed, but save the computation result once the ast is traversed
         * to find the answer to this question.
         *
         *
         */
        private String stringify(final ASTTypeArgument typeArgument, final String filename) {
            return typeArgument.hasAnyDescendantBeenModified() ? stringifyModified(typeArgument)
                : stringifyOriginal(typeArgument, filename);
        }

        private String stringifyOriginal(final Node node, final String filename) { // This can be applied to ANY node

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
