package net.sourceforge.pmd.lang.syntax.parser.context;

import net.sourceforge.pmd.lang.syntax.parser.value.ParsedValue;

public interface ParseContext {
    boolean hasNext();
    ParsedValue parseNext();
    ParsePolicy getParsePolicy();
}
