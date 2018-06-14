/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure.element;

import net.sourceforge.pmd.lang.syntax.context.SyncContext;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operand.MatcherSym;
import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;

public interface StructureElement extends MatcherSym {
    TokenRegion sync(SyncContext syncContext, StructureElement oldSE, TokenRegion oldTokenRegion);
}
