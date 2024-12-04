package de.fernunihagen.dbis.anguillasearch;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** The forward index looks like this:
*   page1 : ["discover", "world", "of", "divine"]
*   page2 : ["explore", "world", "cheese", "manchego"]
 */
public class FwdIndex {
     /** A Map provides the needed functionality and getting the list of strings
     *   by the key is fast.*/
    private Map<Page, List<String>> indexMap = new TreeMap<>();

    /**
     * Add (Page,tokenList) Pair to the forward index.
     * @param page page which should be inserted
     * @param tokenList list of tokens which should be inserted
     * @return the previous list of tokens associated with the page.
     */
    protected List<String> put(final Page page, final List<String> tokenList) {
        return indexMap.put(page, tokenList);
    }
    /**
     * Returns the number of stored pages.
     * @return the number of pages stored in the forward index.
     */
    public int size() {
        return indexMap.size();
    }
}
