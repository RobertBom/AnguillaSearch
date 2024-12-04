package de.fernunihagen.dbis.anguillasearch;

import de.fernunihagen.dbis.records.TokenIDF;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;



/**
 * Indexer Class of the AnguillaSearch project.
 * 
 * Uses the Crawler Class to crawl the specified net. Uses a forward index and
 * a reverse index to provide various functions.
 * 
 */
public class Indexer {
    /** The Crawler provides us with a list of fetched pages.*/
    private Crawler crawler;
    /** A list of pages which is used as data source.*/
    private List<Page> pageList;
    /** Forward index, which stores TFIDF values for every page stored. */
    private VecFwdIndex fwdIndex = new VecFwdIndex();
    /** Reverse Index. */
    private RevIndex revIndex = new RevIndex();
    /** Not initialized, because the size is unknown at this point. 
     * Pair of (token, IDF).
     * Is sorted lexicographically by token attribute.
     */
    private TokenIDF[] tokenIDFVector;
    /** Defines which method should be used to rank the search results.
     * 0 = TF-IDF.
     * 1 = Cosine similarity (default).
     */
    private int searchMode = 1;

    /**
     * Crawls all pages starting from the seed URLs.
     * Initializes the forward and backward index.
     * @param seedURLs   seed URLs the crawler should start from
     */
    Indexer(final String[] seedURLs) {
        crawler = new Crawler(seedURLs);
        crawler.crawl();
        this.pageList = crawler.getCrawledPages();
        buildrevIndex();
        buildTokenVector();
        buildForwardIndex();
    }
    /**
     * Sets the data source for the index to the provided list of pages.
     * Initializes the forward and backward index.
     * @param pageList a list of pages which provides the data for the index
     */
    Indexer(final List<Page> pageList) {
        this.pageList = pageList;
        buildrevIndex();
        buildTokenVector();
        buildForwardIndex();
    }
    /**
     * Builds a forward index of all the pages in the provided list.
     * The values are a tokenized, stopword-removed, lemmatized list of all
     * tokens contained in the page.
     * The forward index looks like this:
     * page1 : ["discover", "world", "of", "divine"]
     * page2 : ["explore", "world", "cheese", "manchego"]
     */
    private void buildForwardIndex() {
        for (Page crawledPage : pageList) {
            fwdIndex.put(crawledPage, crawledPage.getFilteredLemmaList(),
            tokenIDFVector);
        }
    }
    /**
     * Builds a reverse index of all the pages in the provided list.
     * The values are maps of a url and page which contain the token.
     * The reverse index looks like this:
     * "manchego" : [(url1,page1), (url5,page5), (url9,page9)]
     * "exquisite" : [(url2,page2)]
     */
    private void buildrevIndex() {
        // run through all crawled Pages
        for (Page crawledPage : pageList) {
            // run through all token in crawledPage and add them to revIndex.
            for (String token : crawledPage.getFilteredLemmaList()) {
                // get the the page list for current token, null if already in
                // map
                Map<String, Page> curPageList = revIndex.get(token);
                if (curPageList == null) {
                    /* there is no entry for the current token in the reverse
                       index. We need to create a new value.
                    */
                    Map<String, Page> newMap = new TreeMap<>();
                    newMap.put(crawledPage.getURL(), crawledPage);
                    revIndex.put(token, newMap);
                } else {
                    curPageList.put(crawledPage.getURL(), crawledPage);
                }
            }
        }
    }
    private void buildTokenVector() {
        tokenIDFVector = new TokenIDF[revIndex.size()];
        int i = 0;
        for (String token : revIndex.keySet()) {
            tokenIDFVector[i] = new TokenIDF(token, calcIDF(token));
            i++;
        }
    }    
    /**
     * Return a map of <String url, Page p> which of pages which contain the
     * token.
     * @param key   the token of which a map of <String url, Page p> is to be 
     * returned
     * @return  a list of pages, which contain the key. Null if the token is
     * not on any page
     */
    public Map<String, Page> getReverseIndexValues(final String key) {
        return revIndex.get(key);
    }
    /**
     * Print the amount of keys in the forward and revese index.
     */
    public void printInfo() {
        System.out.println("Forward Index has " + fwdIndex.size() 
        + " key-value mappings.");
        System.out.println("Reverse Index has " + revIndex.size() 
        + " key-value mappings.");
    }
    /**
     * Calculates the IDF (Inverted Document Frequency) for a provided token.
     * The formula is: IDF(t) = ln( N / df(t) ).
     * t is the provided token.
     * N is the amount of all crawled pages.
     * df(t) is the amount of pages containing the token t.
     * @param t the token the IDF should be calculated of
     * @return the calculated IDF value
     */
    public double calcIDF(final String t) {
        double n = pageList.size();
        //document frequency df(t)
        if (revIndex.get(t) != null) {
            double dft = revIndex.get(t).size();
            return Math.log(n / dft);
        } else {
            return 0.0;
        }
    }
    /**
     * Calculates the TF (Term Frequency).
     * The formula is: t/d.
     * t is the provided token.
     * d is the amount of tokens on the page.
     * @param t the token used for the calculation
     * @param p the page used for the calculation
     * @return the calculated TF.
     */
    public static double calcTF(final String t, final Page p) {
        double count = 0;
        for (String tokenInPage : p.getFilteredLemmaList()) {
            if (t.equals(tokenInPage)) {
                count++;
            }
        }
        return count / (p.getFilteredLemmaList().size());
    }
    /**
     * Calculates the TF-IDF.
     * The formula is: TF * IDF.
     * @param t the token used for the calculation
     * @param p the page used for the calculation
     * @return the calculated TF-IDF value
     */
    public double calcTFIDF(final String t, final Page p) {
        return calcTF(t, p) * calcIDF(t);
    }
    /**
     * Executes a search for the provided query.
     * Removes the stop words tokenizes and lemmatizes the query.
     * Builds a list of all Pages containing a least one of the tokens.
     * Then it calculates the TF-IDF value for all tokens for a specific page
     * and cumulates them. Does this for every page.
     * Sorts the url of the pages by the cumulated TF-IDF value ascending.
     * @param searchString the searchterms seperated by spaces
     * @param explRankMode specifies which ranking method should be used. 
     * 0 TFIDF, 1 cosine similarity.
     * @return a list of the search results sorted by the score value.
     */
    public List<SearchResult> searchQuery(final String searchString,
                                         final int explRankMode) {
        // TreeSet eliminates duplicates and sorts our Token
        TreeSet<String> searchTokenList = new TreeSet<>(
                                                Parser.tokLem(searchString));
        Set<Page> pageSet = new TreeSet<>();

        // Build a list of all Pages which contains at least one of the search
        // token.
        for (String searchToken : searchTokenList) {
            if (revIndex.get(searchToken) != null) {
                pageSet.addAll(revIndex.get(searchToken).values());
            }
        }
        switch (explRankMode) {
            case 0:
                return rankTFIDF(pageSet, searchTokenList);
            case 1:
                return rankCosineSimilarity(pageSet, searchTokenList);
            default:
                return rankTFIDF(pageSet, searchTokenList);
        }
    }
    /**
     * @param searchString
     * @return list of the search results sorted by the score value.
     */
    public List<SearchResult> searchQuery(final String searchString) {
        return searchQuery(searchString, this.searchMode);
    }
    private List<SearchResult> rankTFIDF(final Set<Page> pageSet, 
                                        final Set<String> searchTokenList) {
        List<SearchResult> searchResults = new ArrayList<>(pageSet.size());
        // Iterate over all Pages, which have searchresults and calulate TF-IDF
        // sum.
        Iterator<Page> iter = pageSet.iterator();
        while (iter.hasNext()) {
            double tfidfSum = 0;
            Page p = iter.next();
            // Iterate over the searchtokens for current page and cumulate
            // TF-IDF
            for (String searchToken : searchTokenList) {
                tfidfSum += calcTFIDF(searchToken, p);
            }
            searchResults.add(new SearchResult(p.getURL(), tfidfSum));
        }

        // In the tokenList.sort line no checkstyle error, 
        //here WhiteSpaceAround!?
        searchResults.sort((a, b) ->  (b.score() > a.score() ? -1 : 1));
        return searchResults;
    }
    private List<SearchResult> rankCosineSimilarity(final Set<Page> pageSet, 
                                    final TreeSet<String> searchTokenList) {
        List<SearchResult> searchResults = new ArrayList<>(pageSet.size());
        int vecL = tokenIDFVector.length;

        // build search vector 
        double[] searchV = new double[vecL];
        Arrays.fill(searchV, 0.0);
        /* We can use the fact that both searchTokenList and tokenIDFVector are
        *  lexicographically sorted. i is used as an index for tokenIDFVector.
        *  Could optimize further using binary search but not a priority right
        *  now.
        */
        int i = 0;
        Iterator<String> stIter = searchTokenList.iterator();
        while (stIter.hasNext() && i < vecL) {
            String curSearchToken = stIter.next();
            // while our current search token is lexicographically greater, we 
            // move forward in the tokenIDFVector
            while (curSearchToken.compareTo(tokenIDFVector[i].token()) > 0 &&
                                                                    i < vecL) {
                i++;
            }
            if (curSearchToken.compareTo(tokenIDFVector[i].token()) == 0) {
                searchV[i] = 1;
            }
        }
        
        // iterate through pages and rank then using cosine similarity.
        Iterator<Page> pIter = pageSet.iterator();
        while (pIter.hasNext()) {
            Page curPage = pIter.next();
            double curScore = -1; // debug value

            curScore = cosineSimilarity(searchV, 
                                        fwdIndex.getTFIDFVector(curPage));
            searchResults.add(new SearchResult(curPage.getURL(), curScore));
        }
        // sort the search results by score ascending.
        searchResults.sort((a, b) ->  (b.score() > a.score() ? -1 : 1));


        return searchResults;
    }
    /**
     * Executes a search for provided query.
     * Uses searchQuery() and just returns the of sorted URLs without the
     * TF-IDF values.
     * @param searchString the searchterms seperated by spaces
     * @return a sorted list of urls containing at least on the searchterms,
     * sorted by the cumulated TF-IDF value.
     */
    public List<String> search(final String searchString) {
        List<SearchResult> searchResults = searchQuery(searchString);
        List<String> urlList = new ArrayList<>(searchResults.size());

        // extract the URL list of the SearchResult list.
        for (SearchResult searchResult : searchResults) {
            urlList.add(searchResult.url());
        }
        return urlList;
    }

    /**
     * Analyzes the reverse index to build a list of the most common tokens.
     * Useful to build a stopword list.
     */
    public void printMostCommonTokens() {
        // Represents the amount of tokens the reverse index contains */
        int size = revIndex.size();
        // How many results we want to print  */
        final int resToPrint = 25;
        List<TokenCount> tokenList = new ArrayList<>(size);
        for (String key : revIndex.keySet()) {
            tokenList.add(new TokenCount(key));
        }
        // In Line 221
        tokenList.sort((a, b) ->  b.count-a.count);
        for (int i = 0; i < resToPrint && i < tokenList.size(); i++) {
            System.out.format("Token: %-20s Count: %d%n", 
            tokenList.get(i).token, tokenList.get(i).count);
        }
    }
    /**
     * Simple class to represent a token and it's associated count.
     */
    private class TokenCount {
        /** The Token. */
        private String token;
        /** How often the token was counted. */
        private int count;

        TokenCount(final String token) {
            this.token = token;
            count = revIndex.get(token).size();
        }
    }
    /**
     * Calculates the dot product of two vectors. Throws 
     * IllegalArgumentException if the dimensions do not match.
     * dot product is defined as: \sum_{i=1}^n a_i \cdot a_b.
     * n is the dimension of vector a and b.
     * @param a vector a
     * @param b vector b
     * @return Dot product of a and b.
     */
    static double dotProduct(final double[] a, final double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vector dimension not the same");
        } else {
            double result = 0;
            for (int i = 0; i < a.length; i++) {
                result += a[i] * b[i];
            }
            return result;
        }
    }
    /**
     * Calculates the euclidian norm of the vector in the argument.
     * The euclidian norm is defined as:
     * \sqrt{\sum_{i=1}^n a_i^2}.
     * @param a vector of which the euclidian norm should be calculated
     * @return the euclidian norm of the vector.
     */
    static double calcEuclidianNorm(final double[] a) {
        double quadSum = 0;
        // sum_{i=1}^n a_i^2
        for (int i = 0;  i < a.length; i++) {
            quadSum += a[i] * a[i];
        }
        return Math.sqrt(quadSum);
    }
    /**
     * Calculates the cosine similarity between two vectors.
     * The cosine similarity is defined as:
     * \frac{a \cdot b} {||a|| \cdot ||b||}.
     * @param a vector a
     * @param b vector b
     * @return cosine similarity of a and b.
     */
    static double cosineSimilarity(final double[] a, final double[] b) {
        return dotProduct(a, b) / 
        (calcEuclidianNorm(a) * calcEuclidianNorm(b));
    }

}
