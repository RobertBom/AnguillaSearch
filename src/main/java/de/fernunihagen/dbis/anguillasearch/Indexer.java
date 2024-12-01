package de.fernunihagen.dbis.anguillasearch;

import java.util.ArrayList;
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
    /** A Map provides the needed functionality and getting the list of strings
     *  by the key is fast.*/
    private Map<Page, List<String>> forwardIndex = new TreeMap<>();
    /** A Map provides the needed functionality for the reverseIndex.
       The value a is map again, with url as the key and the corresponding Page
       object.*/
    private Map<String, Map<String, Page>> reverseIndex = new TreeMap<>();


    /**
     * Crawls all pages starting from the seed URLs.
     * Initializes the forward and backward index.
     * @param seedURLs   seed URLs the crawler should start from
     */
    Indexer(final String[] seedURLs) {
        crawler = new Crawler(seedURLs);
        crawler.crawl();
        this.pageList = crawler.getCrawledPages();
        buildForwardIndex(pageList);
        buildReverseIndex(pageList);
    }
    /**
     * Sets the data source for the index to the provided list of pages.
     * Initializes the forward and backward index.
     * @param pageList a list of pages which provides the data for the index
     */
    Indexer(final List<Page> pageList) {
        this.pageList = pageList;
        buildForwardIndex(pageList);
        buildReverseIndex(pageList);
    }
    /**
     * Builds a forward index of all the pages in the provided list.
     * The values are a tokenized, stopword-removed, lemmatized list of all
     * tokens contained in the page.
     * The forward index looks like this:
     * page1 : ["discover", "world", "of", "divine"]
     * page2 : ["explore", "world", "cheese", "manchego"]
     * @param crawledPages a list of crawled pages to build the forward index
     */
    private void buildForwardIndex(final List<Page> crawledPages) {
        for (Page crawledPage : crawledPages) {
            forwardIndex.put(crawledPage, crawledPage.getFilteredLemmaList());
        }
    }
    /**
     * Builds a reverse index of all the pages in the provided list.
     * The values are maps of a url and page which contain the token.
     * The reverse index looks like this:
     * "manchego" : [(url1,page1), (url5,page5), (url9,page9)]
     * "exquisite" : [(url2,page2)]
     * @param crawledPages a list of crawled pages to build the reverse index
     */
    private void buildReverseIndex(final List<Page> crawledPages) {
        //run through all crawled Pages
        for (Page crawledPage : crawledPages) {
            //run through all token in crawledPage and add them to reverseIndex.
            for (String token : crawledPage.getFilteredLemmaList()) {
                //the page list for current token, null if already in map
                Map<String, Page> curPageList = reverseIndex.get(token);
                if (curPageList == null) {
                    Map<String, Page> newMap = new TreeMap<>();
                    newMap.put(crawledPage.getURL(), crawledPage);
                    reverseIndex.put(token, newMap);
                } else {
                    curPageList.put(crawledPage.getURL(), crawledPage);
                }
            }
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
        return reverseIndex.get(key);
    }
    /**
     * Print the amount of keys in the forward and revese index.
     */
    public void printInfo() {
        System.out.println("Forward Index has " + forwardIndex.size() 
        + " key-value mappings.");
        System.out.println("Reverse Index has " + reverseIndex.size() 
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
        double n = forwardIndex.size();
        //document frequency df(t)
        if (reverseIndex.get(t) != null) {
            double dft = reverseIndex.get(t).size();
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
    public double calcTF(final String t, final Page p) {
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
     * @return a list of the search results sorted by the cumulated TF-IDF
     * value.
     */
    public List<SearchResult> searchQuery(final String searchString) {
        List<String> searchTokenList = Parser.tokLem(searchString);
        Set<Page> pageSet = new TreeSet<>();
        List<SearchResult> searchResults;

        // Build a list of all Pages which contains at least one of the search
        // token.
        for (String searchToken : searchTokenList) {
            if (reverseIndex.get(searchToken) != null) {
                pageSet.addAll(reverseIndex.get(searchToken).values());
            }
        }
        searchResults = new ArrayList<>(pageSet.size());

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
        searchResults.sort((a, b) ->  (b.tfidf() > a.tfidf() ? -1 : 1));
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
        /** Represents the amount of tokens the reverse index contains */
        int size = reverseIndex.size();
        /** How many results we want to print  */
        final int resToPrint = 25;
        List<TokenCount> tokenList = new ArrayList<>(size);
        for (String key : reverseIndex.keySet()) {
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
            count = reverseIndex.get(token).size();
        }
    }

}
