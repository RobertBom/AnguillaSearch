package de.fernunihagen.dbis.anguillasearch;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Collection;
import java.util.HashSet;

public class PageRank {
    /**
     * Inbound link list. URL is used as a key. The value is a set of pages
     * link to the page specified in the key. We use a set, so we treat 
     * multiple outbound links of a page to another as one.
     */
    private Map<String, LinkMapElem> linkMap = new TreeMap<>();
    /** Map of Pageranks, the key is the url and the value the pagerankrating. */
    private Map<String, Double> pageRankMap = new TreeMap<>();
    /** Damping factor of weight between pagerankrating and ranksource values. */
    static final double DAMPING_FACTOR = 0.85;
    /** How much small the difference between pagerank iterations has to be. */
    static final double EPSILON = 0.0001;
    /** Maximum pagerank iterations to prevent a neverending loop. */
    static final int MAXPAGERANKIT = 10000;
    
    PageRank(final Collection<Page> pageList) {
        buildInbLinkList(pageList);
        rankPages();
    }
    /**
     * Builds the map for inbound links.
     * @param pageList the list of pages of which the map for inbound links
     * should be created.
     */
    private void buildInbLinkList(final Collection<Page> pageList) {
        // create a map entry with empty set for every page
        // and the outgoing link count
        for (Page curPage : pageList) {
            LinkMapElem insert = new LinkMapElem(new HashSet<>(),
                                                curPage.getLinks().size());
            linkMap.put(curPage.getURL(), insert);
        }

        // iterate through all pages again, Seed URLs will contain empty set, 
        // since no other site links to them.
        for (final Page curPage : pageList) {
            for (String outLink : curPage.getLinks()) {
                /* curSet can't be null, since the crawler did crawl every
                   every outgoing link. In the loop above we created a set
                   for every page. */
                Set<String> curSet = linkMap.get(outLink).inBoundLinkSet();
                // curPage links to the page outlink. curSet represents all
                // inbound links to outlink.
                curSet.add(curPage.getURL());
            }
        }
    }
    /**
     * ranks all pages with the Pagerank algorithm. 
     * The inbound linkList has to be build before.
     */
    private void rankPages() {
        // initialize all pages with the pagerank of 1/N, where N is the number
        // of total sites. Also functions as our ranksource value.
        double n = 1.0 / linkMap.size();
        for (String key : linkMap.keySet()) {
            pageRankMap.put(key, n);
        }
        int iterationCount = 0;
        double delta = 0;
        // Calculate new PageRanks till the cumulated absolute 
        // delta is smaller than EPSILON
        do {
            Map<String, Double> newPageRankMap = new TreeMap<>();
            delta = 0;
            // iterate through all pages.
            for (Map.Entry<String, Double> prEntry : pageRankMap.entrySet()) {
                double oldPageR = prEntry.getValue();
                double newPageR = 0;
                // calculate the Pagerank for current page.
                // get a Set of all pages linking to pURL
                Set<String> linksToCurPageSet = linkMap.get(prEntry.getKey())
                                                            .inBoundLinkSet();
                // iterate through every page to links tu curPage and cumulate
                // the new Pagerank value
                for (String linksToCurPage : linksToCurPageSet) {
                    double prj = pageRankMap.get(linksToCurPage);
                    int cj = linkMap.get(linksToCurPage).outBoundLinks();
                    newPageR += DAMPING_FACTOR * (prj / cj);
                }
                // add Ranksource
                newPageR += (1 - DAMPING_FACTOR) * n;
                delta += Math.abs(oldPageR - newPageR);
                // save new PageRank to out new map
                newPageRankMap.put(prEntry.getKey(), newPageR);
                //System.out.format("URL: %-50s old PR: %5f new PR: %5f%n", curPage, oldPageR, newPageR);
                
            }
            // switch old pageRanks with new pageRanks
            pageRankMap = newPageRankMap;
            iterationCount++;
        } while (delta > EPSILON && iterationCount < MAXPAGERANKIT);
        // System.out.println(iterationCount);
        double pageRankSum = pageRankMap.values()
                                .stream()
                                .mapToDouble(Double::doubleValue)
                                .sum();
        double avg = pageRankSum / pageRankMap.size();
        // System.out.println(pageRankMap.size());
    }
    /**
     * Returns the total amount of inbound links.
     * @return the total amound of inbound links.
     */
    protected int getTotalInBoundLinks() {
        int countInBound = 0;
        for (LinkMapElem curElem : linkMap.values()) {
            // curElem.inBoundLinkSet() is the Set of all inbound links for
            // current map element. size() is the count of inbound links to
            // that element.
            countInBound += curElem.inBoundLinkSet().size();
        }   
        return countInBound;
    }
    /**
     * Returns the number of inbound links to the specified site. Returns -1 if
     * the url is invalid.
     * @param url the url of which the inbound link count should be returned
     * @return the number of inbound links to the site. -1 if the url provided
     * was not found
     */
    protected int getNumInboundLinks(final String url) {
        Set<String> curSet = linkMap.get(url).inBoundLinkSet();
        if (curSet == null) {
            return -1;
        } else {
            return curSet.size();
        }
    }
    /**
     * Returns a map of the pageranks.
     * The key is the url of the page and the value the according pagerank.
     * @return a map of the pageranks.
     */
    protected Map<String, Double> getPageRankMap() {
        return pageRankMap;
    }
    private record LinkMapElem (
        Set<String> inBoundLinkSet,
        int outBoundLinks
    ) { }

}
