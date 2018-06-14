/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.value;

import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.GenericToken;

public interface ParsedValueMatcher {
    boolean doesMatch(GenericToken parsedToken);
    boolean doesMatch(AbstractNode parsedChild);
}
