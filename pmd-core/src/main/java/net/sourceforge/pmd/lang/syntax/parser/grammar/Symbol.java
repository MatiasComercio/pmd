/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.syntax.parser.grammar;

import net.sourceforge.pmd.lang.syntax.parser.NFAFragment;

public interface Symbol {
    /*
     * areStatesMandatory indicates whether symbols in this fragment will be present in every structure.
     * Basically, areStatesMandatory will be true if this symbol is part of any OperatorSym except for the ConcatenationSym.
     */
    NFAFragment toNFAFragment(boolean areStatesMandatory);
}
