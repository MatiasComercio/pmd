/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sourceforge.pmd.autofix.RewritableNode;
import net.sourceforge.pmd.autofix.rewriteevents.RewriteEvent;
import net.sourceforge.pmd.lang.ast.xpath.Attribute;
import net.sourceforge.pmd.lang.ast.xpath.DocumentNavigator;
import net.sourceforge.pmd.lang.dfa.DataFlowNode;

public abstract class AbstractNode implements RewritableNode {

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
    private GenericToken firstToken;
    private GenericToken lastToken;
    private AST ast; // xnow: TODO somewhere (idea: visitor right before RuleSets.apply execution)

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
        setChild(child, index);
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

    /**
     * Subclasses should implement this method to return a name usable with
     * XPathRule for evaluating Element Names.
     */
    @Override
    public abstract String toString();

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
        return this.getImage() != null && this.getImage().equals(image);
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
        return (T) parentNode;
    }


    @Override
    public <T> List<T> getParentsOfType(Class<T> parentType) {
        List<T> parents = new ArrayList<>();
        Node parentNode = jjtGetParent();
        while (parentNode != null) {
            if (parentType.isInstance(parentNode)) {
                parents.add((T) parentNode);
            }
            parentNode = parentNode.jjtGetParent();
        }
        return parents;
    }


    @Override
    public <T> List<T> findDescendantsOfType(Class<T> targetType) {
        List<T> list = new ArrayList<>();
        findDescendantsOfType(this, targetType, list, true);
        return list;
    }


    @Override
    public <T> void findDescendantsOfType(Class<T> targetType, List<T> results, boolean crossBoundaries) {
        findDescendantsOfType(this, targetType, results, crossBoundaries);
    }

    private static <T> void findDescendantsOfType(Node node, Class<T> targetType, List<T> results,
                                                  boolean crossFindBoundaries) {

        if (!crossFindBoundaries && node.isFindBoundary()) {
            return;
        }

        int n = node.jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            Node child = node.jjtGetChild(i);
            if (child.getClass() == targetType) {
                results.add((T) child);
            }

            findDescendantsOfType(child, targetType, results, crossFindBoundaries);
        }
    }


    @Override
    public <T> List<T> findChildrenOfType(Class<T> targetType) {
        List<T> list = new ArrayList<>();
        int n = jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            Node child = jjtGetChild(i);
            if (targetType.isInstance(child)) {
                list.add((T) child);
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
            if (child.getClass() == childType) {
                return (T) child;
            }
        }
        return null;
    }

    private static <T> T getFirstDescendantOfType(Class<T> descendantType, Node node) {
        int n = node.jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            Node n1 = node.jjtGetChild(i);
            if (n1.getClass() == descendantType) {
                return (T) n1;
            }
            T n2 = getFirstDescendantOfType(descendantType, n1);
            if (n2 != null) {
                return n2;
            }
        }
        return null;
    }


    @Override
    public final <T> boolean hasDescendantOfType(Class<T> type) {
        return getFirstDescendantOfType(type) != null;
    }

    /**
     * @param types
     * @return boolean
     */
    public final boolean hasDecendantOfAnyType(Class<?>... types) {
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

    // xnow: RewritableNode interface implementations

    @Override
    public void insertChild(final Node newChild, final int index) {
        preInsertChild(newChild, index);
        internalInsertChild(newChild, index);
        postInsertChild(newChild, index);
    }


    /**
     * Set the given newChild at the given index.
     * - If index < 0, IllegalArgumentException is thrown
     * - Else, if index >= jjtGetNumChildren, the children array is extended so as to perform the insertion.
     * - Else, the newChild at the given index, if any, is detached form this parent and replaced by the given newChild.
     * @param newChild The newChild to be set.
     * @param index The position where to set the given newChild.
     * @throws IllegalArgumentException If the index is negative.
     */
    @Override // xnow: review comments & update documentation
    public void setChild(final Node newChild, final int index) {
        if (children == null || index >= children.length) {
            // It is an insert as it is impossible that there may be any old child node at the given index
            insertChild(newChild, index);
        } else {
            replaceChild(newChild, index);
        }
    }

    private void replaceChild(final Node newChild, final int index) {
        final Node oldChild = preReplaceChild(newChild, index);
        internalReplaceChild(oldChild, newChild, index);
        postReplaceChild(oldChild, newChild, index);
    }

    @Override
    public void removeChild(final int index) {
        final Node oldChild = preRemoveChild(index);
        internalRemoveChild(oldChild, index);
        postRemoveChild(oldChild, index);
    }

    @Override
    public void remove() {
        // Detach current node of its parent, if any
        if (parent != null && parent instanceof RewritableNode) {
            ((RewritableNode) parent).removeChild(jjtGetChildIndex());
        }
    }

    @Override
    public boolean haveChildrenChanged() {
        return ast.hasRewriteEvents(this);
    }

    @Override
    public RewriteEvent[] getChildrenRewriteEvents() {
        return ast.getRewriteEvents(this);
    }

    private void internalReplaceChild(final Node oldChild, final Node child, final int index) {
        children[index] = child;
        child.jjtSetChildIndex(index);
        // Attach new child node to its parent
        child.jjtSetParent(this);
        // Detach old child node, if any, of its parent
        if (oldChild != null) {
            oldChild.jjtSetParent(null);
        }
    }

    private void internalInsertChild(final Node newChild, final int index) {
        children[index] = newChild;
        newChild.jjtSetChildIndex(index);
        newChild.jjtSetParent(this);
    }

    private void makeSpaceToInsertChild(final int index) {
        if (children == null) {
            children = new Node[index + 1];
            return; // The children's array is already empty, so there is space for the new child
        }

        // If there is no child in this index, there is space for the new child
        if (index < children.length && children[index] == null) {
            return;
        }

        // If there is already a child in this index, let's shift them all to the right
        Node[] newChildren = new Node[children.length + 1]; // xnow: this should be the index or sth of that sort (look the commented code below)
        System.arraycopy(children, 0, newChildren, 0, index);
        System.arraycopy(children, index, newChildren, index + 1, children.length - index);
        children = newChildren;
        // Update indexes & right-shifted children indexes
        for (int i = index + 1; i < jjtGetNumChildren(); i++) {
            jjtGetChild(i).jjtSetChildIndex(i);
        }
    }

    /*
        if (children == null) { // insert, for sure
            children = new Node[index + 1];
        } else if (index >= children.length) { // insert, for sure
            Node[] newChildren = new Node[index + 1];
            System.arraycopy(children, 0, newChildren, 0, children.length);
            children = newChildren;
        } else { // it must be a replace; so, the problem is that we cannot insert a new child in the middle of other two
            oldChild = children[index];
        }
     */

    private void internalRemoveChild(final Node oldChild, final int index) {
        // Remove the child at the given index
        children = ArrayUtils.remove(children, index);
        // Update the remaining & left-shifted children indexes
        for (int i = childIndex; i < jjtGetNumChildren(); i++) {
            jjtGetChild(i).jjtSetChildIndex(i);
        }

        // Detach old child node of its parent, if any
        oldChild.jjtSetParent(null);
    }

    private void validateNewChild(final Node newChild, final int index) {
        Objects.requireNonNull(newChild);
        validateNonNegative(index);
    }

    private void validateNonNegative(final int n) {
        if (n < 0) {
            throw new IllegalArgumentException("index cannot be negative");
        }
    }

    private void validateRemoveChild(final int index) {
        if (0 > index || index >= jjtGetNumChildren()) {
            throw new IllegalArgumentException("index should be <= 0 and < jjtGetNumChildren()");
        }
    }

    private void preInsertChild(final Node newChild, final int index) {
        validateNewChild(newChild, index);
        this.ast.preInsertChild(this, newChild, index);
        makeSpaceToInsertChild(index); // Ensure that the given index position is empty
    }

    private void postInsertChild(final Node newChild, final int index) {
        this.ast.postInsertChild(this, newChild, index);
    }

    private Node preReplaceChild(final Node newChild, final int index) {
        validateNewChild(newChild, index);
        // Null child may have been caused due to an invalid insertion
        final Node oldChild = children[index];
        this.ast.preReplaceChild(this, oldChild, newChild, index);
        return oldChild;
    }

    private void postReplaceChild(final Node oldChild, final Node newChild, final int index) {
        this.ast.postReplaceChild(this, oldChild, newChild, index);
    }

    private Node preRemoveChild(final int index) {
        validateRemoveChild(index);
        // Null child may have been caused due to an invalid insertion
        final Node oldChild = Objects.requireNonNull(children[index]);
        this.ast.preRemoveChild(this, oldChild, index);
        return oldChild;
    }

    private void postRemoveChild(final Node oldChild, final int index) {
        this.ast.postRemoveChild(this, oldChild, index);
    }
}
