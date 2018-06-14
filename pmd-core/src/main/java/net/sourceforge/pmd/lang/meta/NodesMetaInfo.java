/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.meta;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.lang.ast.AbstractNode;

public abstract class NodesMetaInfo<T extends AbstractNode> {
    private final Class<T> baseNodeClazz;
    private final Map<Class<? extends T>, NodeMetaInfo<? extends T>> nodeMetaInfoPerName;

    protected NodesMetaInfo(final Class<T> baseNodeClazz) {
        this.baseNodeClazz = baseNodeClazz;
        this.nodeMetaInfoPerName = new HashMap<>();
        initializeNodeMetaInfoPerName(nodeMetaInfoPerName);
    }

    @SuppressWarnings("WeakerAccess") // May be customized by subclasses with custom logic.
    protected void initializeNodeMetaInfoPerName(final Map<Class<? extends T>, NodeMetaInfo<? extends T>> nodeMetaInfoPerName) {
        final String[] namesArray = getAllClassesNames();
        final String nodePrefix = getNodePrefix();
        final String nodePackage = getNodePackage();
        for (int nodeKind = 0; nodeKind < namesArray.length; nodeKind++) {
            final String baseName = namesArray[nodeKind];
            final String className = nodePackage + "." + nodePrefix + baseName;
            final Class<? extends T> clazz;
            try {
                clazz = Class.forName(className).asSubclass(baseNodeClazz);
            } catch (final ClassNotFoundException e) {
                handleClassNotFound(e, baseName);
                continue;
            }
            nodeMetaInfoPerName.put(clazz, new NodeMetaInfo<>(clazz, nodeKind));
        }
    }

    protected abstract void handleClassNotFound(final ClassNotFoundException e, final String classBaseName);

    protected abstract String[] getAllClassesNames();

    protected abstract String getNodePackage();

    protected abstract String getNodePrefix();

    public <C extends T> NodeMetaInfo<C> getInstance(final Class<C> clazz) {
        //noinspection unchecked // We are storing keys of Class<C> with values of NodeMetaInfo<C>.
        return  (NodeMetaInfo<C>) nodeMetaInfoPerName.get(clazz);
    }
}
