package de.fernunihagen.dbis.anguillasearch;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RevIndex {
    /** A Map provides the needed functionality for the reverseIndex.
        The value a is map again, with url as the key and the corresponding
        Page object.*/
    private Map<String, Map<String, Page>> indexMap = new TreeMap<>();

    /**
     * Returns a Map<String, Page> of the specified token.
     * @param key the token of which the Map<String, Page> should be retrieved
     * @return the Map<String, Page> associated with the specified token.
     */
    protected Map<String, Page> get(final String key) {
        return indexMap.get(key);
    }
    /**
     * Inserts the specified pair of 
     * @param token the token, of which a Reverse Index element should be
     * created.
     * @param newPageMap a Map<String, Page> of (URL, Page) pairs which contain
     * the specified token
     * @return the previous Map<String, Page> associated with that token, null
     * if there was nothing associated the token before.
     */
    protected Map<String, Page> put(final String token,final Map<String, Page> newPageMap) {
        return indexMap.put(token, newPageMap);
    }
    /**
     * Returns the number of elements in the Reverse Index.
     * @return the number of elements in the Reverse Index.
     */
    protected int size() {
        return indexMap.size();
    }
    /**
     * Returns a set of all URLs the Reverse Index contains.
     * @return a set of all URLs the Reverse Index contains.
     */
    protected Set<String> keySet() {
        return indexMap.keySet();
    }

}
