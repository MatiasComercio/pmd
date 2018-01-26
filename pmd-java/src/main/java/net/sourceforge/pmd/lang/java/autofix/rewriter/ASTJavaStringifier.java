/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.autofix.rewriter;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter;

public class ASTJavaStringifier extends JavaParserVisitorAdapter {
    private final List<String> textOperations;

    public ASTJavaStringifier(final List<String> pTextOperations) {
        super();
        this.textOperations = pTextOperations;
    }

    // TODO: remove this simple dummy implementation
    @Override
    public Object visit(final JavaNode node, final Object data) {
        textOperations.add(String.format("INSERT - beginLine: <%s>, beginColumn: <%s>, endLine: <%s>, endColumn: <%s>",
            node.getBeginLine(), node.getBeginColumn(), node.getEndLine(), node.getEndColumn()));
        return super.visit(node, data);
    }

    // TODO: implement custom stringifier per node
}
