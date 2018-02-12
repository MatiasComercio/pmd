/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.autofix.rewrite;

public interface RewriteMerger {
    RewriteEvent[] mergeWithInsert(InsertEvent insertEvent);
}
