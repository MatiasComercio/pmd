/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.grammar.operand;

import net.sourceforge.pmd.lang.syntax.parser.context.ParsePolicy;
import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;

public interface MatcherSym extends OperandSym {
    boolean shouldMatch(ParsePolicy policy, final ParsedValue value);
    boolean doesMatch(ParsedValue parsedValue);
}
