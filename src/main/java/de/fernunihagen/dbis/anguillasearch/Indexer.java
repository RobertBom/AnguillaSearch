package de.fernunihagen.dbis.anguillasearch;

import de.fernunihagen.dbis.records.TokenIDF;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Indexer Class of the AnguillaSearch project.
 * 
 * Uses the Crawler Class to crawl the specified net. Uses a forward index and
 * a reverse index to provide various functions.
 * 
 */
public class Indexer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);
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
     * 2 = Combination of PageRank and Cosine similarity.
     */
    private int defRankMode = 2;

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
    public void testPageRank() {
        new PageRank(pageList);
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
        LOGGER.info("Forward Index has {} key-value mappings.", fwdIndex.size());
        LOGGER.info("Reverse Index has {} key-value mappings.", revIndex.size());
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
            case 2:
                return rankPageCosine(pageSet, searchTokenList);
            default:
                return rankTFIDF(pageSet, searchTokenList);
        }
    }
    /**
     * @param searchString
     * @return list of the search results sorted by the score value.
     */
    public List<SearchResult> searchQuery(final String searchString) {
        return searchQuery(searchString, this.defRankMode);
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
        searchResults.sort((a, b) ->  (b.score() < a.score() ? -1 : 1));
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
        // sort the search results by score descending.
        searchResults.sort((a, b) ->  (b.score() < a.score() ? -1 : 1));


        return searchResults;
    }
    public List<SearchResult> rankPageCosine(final Set<Page> filteredPageSet,
                                final TreeSet<String> searchTokenList) {
        List<SearchResult> searchResults = new ArrayList<>(filteredPageSet.size());
        /** How much the normalized cosine scores weigh in contrast to 
         * the corresponding pagerank score */
        final double cosWeight = 0.75;

        // getting data
        List<SearchResult> cosineResults = rankCosineSimilarity(filteredPageSet, searchTokenList);
        PageRank pageRank = new PageRank(pageList);
        // pageRankMap contains the pagerank for ALL pages crawled
        Map<String, Double> allPageRankMap = pageRank.getPageRankMap();
        Map<String, Double> pageRankMap = new TreeMap<>();
        // add ONLY pages to pageRankMap, which show up in our searchresults
        cosineResults.forEach(sr -> pageRankMap.put(sr.url(),
                            allPageRankMap.get(sr.url())));

        /* Since the average Pagerank score in our implementation is highly
        *  dependant on the total amount of pages crawled (it should be around
        *  1/N, N = pages crawled), we calculate the average for both the 
        *  cosine and pageRank average and later use a percentage of the avg
        *  as a score.
        */
        OptionalDouble avgCosineScore = cosineResults.stream()
                    .map(p -> p.score()) //get Stream<double> of scores
                    .mapToDouble(Double::doubleValue) // convert to DoubleStream
                    .average(); // calculate average
        if (!avgCosineScore.isPresent()) {
            throw new IllegalArgumentException(
                "cosineResults has uninitialized score values.");
        }
        OptionalDouble avgPageRankScore = pageRankMap.values()
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .average();
        if (!avgPageRankScore.isPresent()) {
            throw new IllegalArgumentException(
                "PageRankMap has uninitialized score values.");
        }

        //LOGGER.debug("Avg Cosine Score is: {} \tAvg Pagerank Score: {}", avgCosineScore, avgPageRankScore);
        for (SearchResult cosRes : cosineResults) {
            double cosineScore = cosRes.score();
            double pageRankScore = pageRankMap.get(cosRes.url());
            // absolute valules
            // System.out.format("URL: %-40s\tCos Score: %f\tPagerank: %f%n", cosRes.url(), cosineScore, pageRankScore);
            // convert scores to percent scores of average
            cosineScore =  cosineScore / avgCosineScore.getAsDouble();
            pageRankScore = pageRankScore / avgPageRankScore.getAsDouble();

            // relative values
            System.out.format("URL: %-40s\tCos Score: %-3.0f%%\tPagerank: %-3.0f%%%n", cosRes.url(), cosineScore*100, pageRankScore*100);
            double newScore =   cosWeight * cosineScore +
                                (1- cosWeight) * pageRankScore;
            searchResults.add(new SearchResult(cosRes.url(), newScore));
        }
        // Sort the resuls by score.
        searchResults.sort( (a,b) -> (b.score() < a.score() ? -1 : 1));
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
        // uses default defRankMode specified in defRankMode attribute.
        return search(searchString, this.defRankMode);
    }
    /**
     * Executes a search for provided query.
     * Uses searchQuery() and just returns the of sorted URLs without the
     * TF-IDF values.
     * @param searchString the searchterms seperated by spaces
     * @return a sorted list of urls containing at least on the searchterms,
     * sorted by the cumulated TF-IDF value.
     */
    public List<String> search(final String searchString, final int explRankMode) {
        List<SearchResult> searchResults = searchQuery(searchString, explRankMode);
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
    public void printSearchResults(List<SearchResult> searchResults) {
        for (SearchResult sr : searchResults) {
            System.out.format("URL: %-35s\tScore: %f%n", sr.url(), sr.score());
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
