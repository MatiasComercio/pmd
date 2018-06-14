/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.fixes.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.java.ast.AbstractJavaNode;
import net.sourceforge.pmd.lang.java.ast.JavaParserTreeConstants;
import net.sourceforge.pmd.lang.meta.NodesMetaInfo;

public class JavaNodesMetaInfo extends NodesMetaInfo<AbstractJavaNode> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaNodesMetaInfo.class);

    // Picked from Java.jjt
    private static final String NODE_PACKAGE = "net.sourceforge.pmd.lang.java.ast";
    private static final String NODE_PREFIX = "AST";
    private static final String INVALID_CLASS_BASE_NAME = "void";

    public JavaNodesMetaInfo() {
        super(AbstractJavaNode.class);
    }

    @Override
    protected void handleClassNotFound(final ClassNotFoundException e, final String classBaseName) {
        if (!classBaseName.equals(INVALID_CLASS_BASE_NAME)) {
            LOGGER.error("Class `{}` not found when creating nodes meta info. Aborting...", classBaseName);
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected String[] getAllClassesNames() {
        return JavaParserTreeConstants.jjtNodeName;
    }

    @Override
    protected String getNodePackage() {
        return NODE_PACKAGE;
    }

    @Override
    protected String getNodePrefix() {
        return NODE_PREFIX;
    }
}
