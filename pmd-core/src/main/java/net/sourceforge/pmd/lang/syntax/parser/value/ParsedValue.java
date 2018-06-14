/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.value;

import net.sourceforge.pmd.lang.syntax.structure.TokenRegion;

public interface ParsedValue {
    boolean doesMatch(ParsedValueMatcher matcher);
    TokenRegion getTokenRegion();
}
