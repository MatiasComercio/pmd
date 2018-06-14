/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sourceforge.pmd.PMDVersion;
import net.sourceforge.pmd.lang.ast.xpath.Attribute;
import net.sourceforge.pmd.lang.ast.xpath.AttributeAxisIterator;
import net.sourceforge.pmd.lang.ast.xpath.DocumentNavigator;
import net.sourceforge.pmd.lang.dfa.DataFlowNode;
import net.sourceforge.pmd.lang.syntax.NodeSyntax;
import net.sourceforge.pmd.lang.syntax.structure.Structure;
import net.sourceforge.pmd.util.GenericTokens;


/**
 * Base class for all implementations of the Node interface.
 */
public abstract class AbstractNode implements Node {

    private static final Logger LOG = Logger.getLogger(AbstractNode.class.getName());


    protected Node parent;
    protected Node[] children;
    protected int childIndex;
    protected int id;

    private String image;
    protected int beginLine = -1;
    protected int endLine;
    protected int beginColumn = -1;
    protected int endColumn;
    private DataFlowNode dataFlowNode;
    private Object userData;
    protected GenericToken firstToken;
    protected GenericToken lastToken;

    private boolean selfSyncRequired = false;
    private boolean childrenSyncRequired = false;
    private Structure structure;

    public AbstractNode(int id) {
        this.id = id;
    }

    public AbstractNode(int id, int theBeginLine, int theEndLine, int theBeginColumn, int theEndColumn) {
        this(id);

        beginLine = theBeginLine;
        endLine = theEndLine;
        beginColumn = theBeginColumn;
        endColumn = theEndColumn;
    }

    public boolean isSingleLine() {
        return beginLine == endLine;
    }

    @Override
    public void jjtOpen() {
        // to be overridden by subclasses
    }

    @Override
    public void jjtClose() {
        // to be overridden by subclasses
    }

    @Override
    public void jjtSetParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public Node jjtGetParent() {
        return parent;
    }

    @Override
    public void jjtAddChild(Node child, int index) {
        // No tokens update required as this method should be called only by JavaCC (parser).
        if (children == null || index >= children.length) {
            addChildNode(index, child);
        } else {
            setChildNode(index, child);
        }
    }

    @Override
    public void jjtSetChildIndex(int index) {
        childIndex = index;
    }

    @Override
    public int jjtGetChildIndex() {
        return childIndex;
    }

    @Override
    public Node jjtGetChild(int index) {
        return children[index];
    }

    @Override
    public int jjtGetNumChildren() {
        return children == null ? 0 : children.length;
    }

    @Override
    public int jjtGetId() {
        return id;
    }

    @Override
    public String getImage() {
        return image;
    }

    @Override
    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean hasImageEqualTo(String image) {
        return Objects.equals(this.getImage(), image);
    }

    @Override
    public int getBeginLine() {
        return beginLine;
    }

    public void testingOnlySetBeginLine(int i) {
        this.beginLine = i;
    }

    @Override
    public int getBeginColumn() {
        if (beginColumn != -1) {
            return beginColumn;
        } else {
            if (children != null && children.length > 0) {
                return children[0].getBeginColumn();
            } else {
                throw new RuntimeException("Unable to determine beginning line of Node.");
            }
        }
    }

    public void testingOnlySetBeginColumn(int i) {
        this.beginColumn = i;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }

    public void testingOnlySetEndLine(int i) {
        this.endLine = i;
    }

    @Override
    public int getEndColumn() {
        return endColumn;
    }

    public void testingOnlySetEndColumn(int i) {
        this.endColumn = i;
    }

    @Override
    public DataFlowNode getDataFlowNode() {
        if (this.dataFlowNode == null) {
            if (this.parent != null) {
                return parent.getDataFlowNode();
            }
            return null; // TODO wise?
        }
        return dataFlowNode;
    }

    @Override
    public void setDataFlowNode(DataFlowNode dataFlowNode) {
        this.dataFlowNode = dataFlowNode;
    }


    @Override
    public Node getNthParent(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        Node result = this.jjtGetParent();
        for (int i = 1; i < n; i++) {
            if (result == null) {
                return null;
            }
            result = result.jjtGetParent();
        }
        return result;
    }


    @Override
    public <T> T getFirstParentOfType(Class<T> parentType) {
        Node parentNode = jjtGetParent();
        while (parentNode != null && !parentType.isInstance(parentNode)) {
            parentNode = parentNode.jjtGetParent();
        }
        return parentType.cast(parentNode);
    }


    @Override
    public <T> List<T> getParentsOfType(Class<T> parentType) {
        List<T> parents = new ArrayList<>();
        Node parentNode = jjtGetParent();
        while (parentNode != null) {
            if (parentType.isInstance(parentNode)) {
                parents.add(parentType.cast(parentNode));
            }
            parentNode = parentNode.jjtGetParent();
        }
        return parents;
    }

    @SafeVarargs
    @Override
    public final <T> T getFirstParentOfAnyType(Class<? extends T>... parentTypes) {
        Node parentNode = jjtGetParent();
        while (parentNode != null) {
            for (Class<? extends T> c : parentTypes) {
                if (c.isInstance(parentNode)) {
                    return c.cast(parentNode);
                }
            }
            parentNode = parentNode.jjtGetParent();
        }
        return null;
    }

    @Override
    public <T> List<T> findDescendantsOfType(Class<T> targetType) {
        List<T> list = new ArrayList<>();
        findDescendantsOfType(this, targetType, list, false);
        return list;
    }

    // TODO : Add to Node interface in 7.0.0
    public <T> List<T> findDescendantsOfType(final Class<T> targetType, final boolean crossBoundaries) {
        final List<T> list = new ArrayList<>();
        findDescendantsOfType(this, targetType, list, crossBoundaries);
        return list;
    }

    @Override
    public <T> void findDescendantsOfType(Class<T> targetType, List<T> results, boolean crossBoundaries) {
        findDescendantsOfType(this, targetType, results, crossBoundaries);
    }

    private static <T> void findDescendantsOfType(Node node, Class<T> targetType, List<T> results,
            boolean crossFindBoundaries) {

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            if (child.getClass() == targetType) {
                results.add(targetType.cast(child));
            }

            if (crossFindBoundaries || !child.isFindBoundary()) {
                findDescendantsOfType(child, targetType, results, crossFindBoundaries);
            }
        }
    }


    @Override
    public <T> List<T> findChildrenOfType(Class<T> targetType) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < jjtGetNumChildren(); i++) {
            Node child = jjtGetChild(i);
            if (targetType.isInstance(child)) {
                list.add(targetType.cast(child));
            }
        }
        return list;
    }

    @Override
    public boolean isFindBoundary() {
        return false;
    }

    @Override
    public Document getAsDocument() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
            appendElement(document);
            return document;
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }
    }

    protected void appendElement(org.w3c.dom.Node parentNode) {
        DocumentNavigator docNav = new DocumentNavigator();
        Document ownerDocument = parentNode.getOwnerDocument();
        if (ownerDocument == null) {
            // If the parentNode is a Document itself, it's ownerDocument is
            // null
            ownerDocument = (Document) parentNode;
        }
        String elementName = docNav.getElementName(this);
        Element element = ownerDocument.createElement(elementName);
        parentNode.appendChild(element);
        for (Iterator<Attribute> iter = docNav.getAttributeAxisIterator(this); iter.hasNext();) {
            Attribute attr = iter.next();
            element.setAttribute(attr.getName(), attr.getStringValue());
        }
        for (Iterator<Node> iter = docNav.getChildAxisIterator(this); iter.hasNext();) {
            AbstractNode child = (AbstractNode) iter.next();
            child.appendElement(element);
        }
    }


    @Override
    public <T> T getFirstDescendantOfType(Class<T> descendantType) {
        return getFirstDescendantOfType(descendantType, this);
    }


    @Override
    public <T> T getFirstChildOfType(Class<T> childType) {
        int n = jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            Node child = jjtGetChild(i);
            if (childType.isInstance(child)) {
                return childType.cast(child);
            }
        }
        return null;
    }


    private static <T> T getFirstDescendantOfType(final Class<T> descendantType, final Node node) {
        final int n = node.jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            Node n1 = node.jjtGetChild(i);
            if (descendantType.isAssignableFrom(n1.getClass())) {
                return descendantType.cast(n1);
            }
            if (!n1.isFindBoundary()) {
                final T n2 = getFirstDescendantOfType(descendantType, n1);
                if (n2 != null) {
                    return n2;
                }
            }
        }
        return null;
    }


    @Override
    public final <T> boolean hasDescendantOfType(Class<T> type) {
        return getFirstDescendantOfType(type) != null;
    }


    /**
     * Returns true if this node has a descendant of any type among the provided types.
     *
     * @param types Types to test
     *
     * @deprecated Use {@link #hasDescendantOfAnyType(Class[])}
     */
    @Deprecated
    public final boolean hasDecendantOfAnyType(Class<?>... types) {
        return hasDescendantOfAnyType(types);
    }


    /**
     * Returns true if this node has a descendant of any type among the provided types.
     *
     * @param types Types to test
     */
    public final boolean hasDescendantOfAnyType(Class<?>... types) {
        // TODO consider implementing that with a single traversal!
        // hasDescendantOfType could then be a special case of this one
        // But to really share implementations, getFirstDescendantOfType's
        // internal helper could have to give up some type safety to rely
        // instead on a getFirstDescendantOfAnyType, then cast to the correct type
        for (Class<?> type : types) {
            if (hasDescendantOfType(type)) {
                return true;
            }
        }
        return false;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<Node> findChildNodesWithXPath(String xpathString) throws JaxenException {
        return new BaseXPath(xpathString, new DocumentNavigator()).selectNodes(this);
    }


    @Override
    public boolean hasDescendantMatchingXPath(String xpathString) {
        try {
            return !findChildNodesWithXPath(xpathString).isEmpty();
        } catch (JaxenException e) {
            throw new RuntimeException("XPath expression " + xpathString + " failed: " + e.getLocalizedMessage(), e);
        }
    }


    @Override
    public Object getUserData() {
        return userData;
    }


    @Override
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public GenericToken jjtGetFirstToken() {
        return firstToken;
    }

    public void jjtSetFirstToken(GenericToken token) {
        this.firstToken = token;
    }

    public GenericToken jjtGetLastToken() {
        return lastToken;
    }

    public void jjtSetLastToken(GenericToken token) {
        this.lastToken = token;
    }

    @Override
    public void removeChildAtIndex(final int childIndex) {
        if (0 <= childIndex && childIndex < jjtGetNumChildren()) {
            // Remove the child at the given index
            children = ArrayUtils.remove(children, childIndex);
            // Update the remaining & left-shifted children indexes
            for (int i = childIndex; i < jjtGetNumChildren(); i++) {
                jjtGetChild(i).jjtSetChildIndex(i);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * <p>This default implementation adds compatibility with the previous
     * way to get the xpath node name, which used {@link Object#toString()}.
     *
     * <p>Please override it. It may be removed in a future major version.
     */
    @Override
    // @Deprecated // FUTURE 7.0.0 make abstract
    public String getXPathNodeName() {
        LOG.warning("getXPathNodeName should be overriden in classes derived from AbstractNode. "
                            + "The implementation is provided for compatibility with existing implementors,"
                            + "but could be declared abstract as soon as release " + PMDVersion.getNextMajorRelease()
                            + ".");
        return toString();
    }


    /**
     *
     *
     * @deprecated The equivalence between toString and a node's name could be broken as soon as release 7.0.0.
     *  Use getXPathNodeName for that purpose. The use for debugging purposes is not deprecated.
     */
    @Deprecated
    @Override
    public String toString() {
        return getXPathNodeName();
    }


    @Override
    public Iterator<Attribute> getXPathAttributesIterator() {
        return new AttributeAxisIterator(this);
    }

    @Override
    public void addChild(final Node newChild) {
        addChild(jjtGetNumChildren(), newChild);
    }

    @Override
    public void addChild(final int index, final Node newChild) {
        validateIndex(index, jjtGetNumChildren());
        validateNonNullRoot(newChild);
        syncRequired();
        addChildNode(index, newChild);
    }

    @Override
    public Node setChild(final int index, final Node newChild) {
        validateIndex(index, jjtGetNumChildren() - 1);
        validateNonNullRoot(newChild);
        syncRequired();
        return setChildNode(index, newChild);
    }

    @Override
    public Node removeChild(final int index) {
        validateIndex(index, jjtGetNumChildren() - 1);
        syncRequired();
        return removeChildNode(index);
    }

    /*
     * xaf: this should be public as AccessNode should inform/require sync on parents for modifiers.
     *  See AbstractSimpleJavaAccessNode.setModifier.
     */
    public final void syncRequired() {
        if (selfSyncRequired && childrenSyncRequired) {
            return;
        }

        // First time scan if needed.
        if (structure == null) {
            structure = getNodeSyntax().scan(this); // FIXME: do sth to avoid unchecked call.
        }

        // Mark me as needing sync.
        selfSyncRequired = true;
        // My added children may need sync too => mark to check to sync them too.
        childrenSyncRequired = true;

        // Mark all my branch to inform that I need to be synced.
        Node currAncestor = parent;
        while (currAncestor != null) {
            AbstractNode aCurrAncestor = requireAbstractNode(currAncestor);
            if (aCurrAncestor.childrenSyncRequired) {
                return;
            }
            aCurrAncestor.childrenSyncRequired = true;
            currAncestor = currAncestor.jjtGetParent();
        }
    }

    private void validateIndex(final int index, final int maxValidIndex) {
        if (index < 0 || index > maxValidIndex) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void validateNonNullRoot(final Node newChild) {
        if (newChild == null || newChild.jjtGetParent() != null) {
            throw new IllegalArgumentException("New Child should be a non-null root node");
        }
    }

    // xdoc: without tokens (all *ChildNode operations do not affect tokens).
    // xdoc: no index validation as this method is internal; all validations should be performed previously.
    private void addChildNode(final int index, final Node newChild) {
        makeSpaceToInsertChild(index); // Ensure that the given index position is empty
        children[index] = newChild;
        newChild.jjtSetChildIndex(index);
        newChild.jjtSetParent(this);
    }

    private void makeSpaceToInsertChild(final int index) {
        if (children == null) {
            children = new Node[index + 1];
            return; // The children's array is already empty, so there is space for the new child
        }

        // Let's shift all children to the right
        Node[] newChildren = new Node[Math.max(children.length, index) + 1]; // Create the new children array with the minimum needed capacity
        // Copy all children that will not be right-shifted
        final int numOfNotShiftedChildren = index < children.length ? index : children.length;
        System.arraycopy(children, 0, newChildren, 0, numOfNotShiftedChildren);

        // Right-shift all missing children from index on
        for (int i = index; i < children.length; i++) {
            final Node child = children[i];
            if (child == null) {
                continue;
            }
            final int newIndex = i + 1;
            child.jjtSetChildIndex(newIndex);
            newChildren[newIndex] = child;
        }

        children = newChildren;
    }

    private Node setChildNode(final int index, final Node newChild) {
        // Null child may have been caused due to an invalid insertion
        final Node oldChild = children[index];
        children[index] = newChild;
        newChild.jjtSetChildIndex(index);
        // Attach new child node to its parent
        newChild.jjtSetParent(this);
        // Detach old child node, if any, of its parent
        if (oldChild != null) {
            oldChild.jjtSetParent(null);
        }

        return oldChild;
    }

    private Node removeChildNode(final int index) {
        // Null child may have been caused due to an invalid insertion
        final Node oldChild = requireNonNull(children[index]);
        // Remove the child at the given index
        children = ArrayUtils.remove(children, index);
        // Update the remaining & left-shifted children indexes
        for (int i = index; i < jjtGetNumChildren(); i++) {
            jjtGetChild(i).jjtSetChildIndex(i);
        }

        // Detach old child node of its parent, if any
        oldChild.jjtSetParent(null);
        return oldChild;
    }

    @Override
    public void remove() {
        // Detach current node of its parent, if any
        if (parent != null) {
            parent.removeChild(jjtGetChildIndex());
        }
    }

    @Override
    public void print() {
        syncIfRequired(); // FIXME: [think] add this sync check in all methods dealing with tokens.
        System.out.println(GenericTokens.stringify(firstToken, lastToken));
    }

    @Override
    public void dump(String var1) {
        System.out.println(this.toString(var1));
        if (this.children != null) {
            for (final Node aChildren : this.children) {
                AbstractNode var3 = (AbstractNode) aChildren;
                if (var3 != null) {
                    var3.dump(var1 + " ");
                }
            }
        }

    }

    public String toString(String var1) {
        return var1 + this.toString();
    }

    private void syncIfRequired() {
        if (childrenSyncRequired) {
            if (children != null) {
                for (Node child : children) {
                    requireAbstractNode(child).syncIfRequired();
                }
                childrenSyncRequired = false;
            }
        }

        if (selfSyncRequired) {
            structure = getNodeSyntax().sync(this); // FIXME: do sth to avoid unchecked call.
            selfSyncRequired = false;
        }
    }

    // FIXME: move this logic of `getPrevToken` to a TokenManager structure.
    public GenericToken getPrevToken() {
        return getPrevToken(firstToken);
    }

    public GenericToken getPrevToken(final int aChildIndex) {
        final AbstractNode aChild = requireAbstractNode(children[aChildIndex]); // TODO: validate index?
        return getPrevToken(aChild.firstToken, aChild.childIndex);
    }

    public GenericToken getPrevToken(final AbstractNode anOldChild) {
        return getPrevToken(anOldChild.firstToken, anOldChild.childIndex);
    }

    public GenericToken getPrevToken(final GenericToken token) {
        return getPrevToken(token, -1);
    }

    /*
     * FIXME: check how does this work when token is null.
     * I think we should change getPrevToken(anOldChild) for getPrevToken(anyChild), and do sth like:
     * ft = anyChild.ft
     * ci = anyChild.ci
     * return ft == null ? ci == 0 ? getPrevToken depending on the structure : this.getPrevToken(ci - 1) // FIXME: ximportant!!!
     *                   : this.getPrevToken(ft, ci);
     */
    public GenericToken getPrevToken(final GenericToken token, final int tokensChildIndex) {
        if (token == null) { // This is not a valid token of the tokens list.
            return null;
        }

        GenericToken precedingToken;
        if (tokensChildIndex > 0) {
            final Node prevChild = children[tokensChildIndex - 1];
            precedingToken = requireAbstractNode(prevChild).lastToken;
        } else {
            if (firstToken == token) {
                // If entered here & I'm root => the given token is the first one of the token list.
                precedingToken = parent == null ? null : requireAbstractNode(parent).getPrevToken(token, childIndex);
            } else {
                precedingToken = firstToken;
            }
        }

        while (precedingToken != null && precedingToken.getNext() != token) {
            precedingToken = precedingToken.getNext();
        }

        // return precedingToken;
        // FIXME #2: uncomment the above line and remove the below lines after fixing #1.
        return precedingToken != null ? precedingToken : parent == null ? null : requireAbstractNode(parent).getPrevToken(token, childIndex);
    }

    public AbstractNode requireAbstractNode(final Node node) {
        if (!(node instanceof AbstractNode)) {
            // FIXME: improve this. Maybe move jjt*Token methods to Node interface?
            throw new IllegalStateException("Cannot build region as the given node does not implement token methods");
        }
        return (AbstractNode) node;
    }

    protected NodeSyntax getNodeSyntax() {
        return null;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(final Structure structure) {
        this.structure = structure;
    }
}
