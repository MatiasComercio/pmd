/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.structure.element;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.syntax.parser.grammar.operand.MatcherSym;

public interface TokenSE extends MatcherSym {
    // Return a new token that represents this SE.
    GenericToken newToken();
    String getImage();
}
