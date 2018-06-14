/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure;

import static java.util.Objects.requireNonNull;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.lang.syntax.structure.element.StructureElement;

public class SEInfo {
    private final StructureElement structureElement;
    private final boolean isMandatorySE;
    private TokenRegion tokenRegion;

    private SEInfo(final StructureElement structureElement,
                  final boolean isMandatorySE,
                  final TokenRegion tokenRegion) {
        this.structureElement = structureElement;
        this.isMandatorySE = isMandatorySE;
        this.tokenRegion = tokenRegion;
    }

    public static SEInfo newInstance(final StructureElement structureElement, final boolean isMandatorySE) {
        return newInstance(structureElement, isMandatorySE, TokenRegion.newEmptyInstance());
    }

    public static SEInfo newInstance(final StructureElement structureElement,
                                     final boolean isMandatorySE,
                                     final TokenRegion tokenRegion) {
        return new SEInfo(requireNonNull(structureElement), isMandatorySE, requireNonNull(tokenRegion));
    }

    public TokenRegion getTokenRegion() {
        return tokenRegion;
    }

    public void sync(final SyncContext syncContext) {
        // Sync & Recognize our tokens region in the tokens chain accordingly.
        if (isMandatorySE) {
            syncMandatory(syncContext);
        } else {
            syncOptional(syncContext);
        }
    }

    private void syncMandatory(final SyncContext syncContext) {
        /*
         * This element should be present in the old structure too, and in the same order, i.e.,
         * the next mandatory SE in the old structure should be me.
         * All other elements until this is reached are no longer needed => we remove them.
         */
        SEInfo oldSEInfo = null;
        boolean found = false;
        while (syncContext.hasNextOldSEInfo() && !found) {
            oldSEInfo = syncContext.nextOldSEInfo();
            if (oldSEInfo.isMandatorySE) {
                found = true;
            }
        }

        if (!found || !structureElement.equals(oldSEInfo.structureElement)) {
            throw new IllegalStateException("Expected mandatorySE is not present in the old structure. Check logic.");
        }

        tokenRegion = requireNonNull(
            structureElement.sync(syncContext, oldSEInfo.structureElement, oldSEInfo.tokenRegion)
        );

        syncContext.insert(tokenRegion);
    }

    // xTODO: extract common code with syncMandatory.
    private void syncOptional(final SyncContext syncContext) {
        final SEInfo oldSEInfo = syncContext.peekNextOldSEInfo();
        tokenRegion = requireNonNull(
            structureElement.sync(syncContext, oldSEInfo.structureElement, oldSEInfo.tokenRegion)
        );

        syncContext.insert(tokenRegion);

        if (tokenRegion.equals(oldSEInfo.tokenRegion)) {
            // We have matched the oldSEInfo => we consume it to avoid being removed later (when syncing mandatory).
            syncContext.nextOldSEInfo();
        }
    }

    public StructureElement getSE() {
        return structureElement;
    }

    public void extendRegion(final TokenRegion newTokenRegion) {
        if (newTokenRegion.isEmpty()) {
            return; // No need to extend any region as this new region is empty.
        }

        final GenericToken newFirstToken = tokenRegion.isEmpty() ? newTokenRegion.getFirstToken() : tokenRegion.getFirstToken();
        final GenericToken newLastToken = newTokenRegion.getLastToken();
        tokenRegion = TokenRegion.newInstance(newFirstToken, newLastToken);
    }
}
