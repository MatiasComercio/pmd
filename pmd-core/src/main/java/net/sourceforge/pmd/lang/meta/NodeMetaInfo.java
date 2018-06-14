/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.meta;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;

public class NodeMetaInfo<T extends AbstractNode> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeMetaInfo.class);

    private final Class<? extends T> clazz;
    private final int kind;

    public NodeMetaInfo(final Class<? extends T> clazz, final int kind) {
        this.clazz = clazz;
        this.kind = kind;
    }

    public Class<? extends T> getClazz() {
        return clazz;
    }

    public int getKind() {
        return kind;
    }

    // xdoc: null is returned if there was any error during reflection initialization.
    public final <C extends Node> T newNode() {
        try {
            return clazz.getDeclaredConstructor(Integer.TYPE).newInstance(kind);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.warn("newNode - Could not create instance of type {}. Returning null...", clazz);
            return null;
        }
    }
}
