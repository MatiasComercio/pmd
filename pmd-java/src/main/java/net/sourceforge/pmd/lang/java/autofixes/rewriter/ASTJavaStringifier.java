/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.autofixes.rewriter;

import java.util.List;

import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter;

public class ASTJavaStringifier extends JavaParserVisitorAdapter {
    private final List<String> textOperations;

    public ASTJavaStringifier(final List<String> pTextOperations) {
        super();
        this.textOperations = pTextOperations;
    }

    // xnow TODO: implement custom stringifier
}
