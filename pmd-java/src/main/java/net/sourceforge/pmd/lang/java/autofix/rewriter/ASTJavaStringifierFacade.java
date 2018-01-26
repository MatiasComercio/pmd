/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.autofix.rewriter;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;

public class ASTJavaStringifierFacade {
    public void initializeWith(final ASTCompilationUnit rootNode, final List<String> textOperations) {
        final ASTJavaStringifier visitor = new ASTJavaStringifier(textOperations);
        rootNode.jjtAccept(visitor, null);
    }
}
