package de.fernunihagen.dbis.anguillasearch;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.fernunihagen.dbis.records.TokenIDF;


public class VecFwdIndex extends FwdIndex {
    /** A map which associates all pages with their TFIDF vector.  */
    private Map<Page, double[]> tfidfMap = new TreeMap<>();

    /**
     * 
     * @param page page which should be inserted
     * @param tokenList list of tokens which should be inserted
     * @param tokenIDFVector vector of all tokens of the index with their
     * corresponding IDF-Value
     * @return the previous list of tokens associated with the page.
     */
    protected List<String> put(
            Page page, 
            List<String> tokenList,
            TokenIDF[] tokenIDFVector) {
        // We call the put of the super class and save the return value to
        // return it later.
        List <String> ret =  super.put(page, tokenList);
        // create idfVector with the right size
        double[] tfidfVector = new double[tokenIDFVector.length];

        // calculate the TF-IDF values for the vector.
        for (int i = 0; i < tokenIDFVector.length; i++) {
            double tf = Indexer.calcTF(tokenIDFVector[i].token(), page);
            tfidfVector[i] =tf * tokenIDFVector[i].idf();
        }
        // normalize all tfidfVectors to length 1so we can skip a calculation
        // step later
        tfidfVector = Indexer.normalizeVector(tfidfVector);
        tfidfMap.put(page, tfidfVector);
        return ret;
    }
    /**
     * Get the TFIDF vector for the requested page
     * @param page the page object of which the TFIDF vector should be returned
     * @return the TFIDF vector of the page.
     */
    protected double[] getTFIDFVector(Page page) {
        return tfidfMap.get(page);
    }
}
