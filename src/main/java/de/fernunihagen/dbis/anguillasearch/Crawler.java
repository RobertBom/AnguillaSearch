package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Crawler Class of the AnguillaSearch project.
 * Crawls the specified net and extracts the titles, header and 
 * content of the pages.
 * The net can be specified by a path to a json with the field "Seed-URLs"
 * or by providing a String array with the Seed-URLs directly.
 */
public class Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);
    /** The Number of Links the crawler has found. */
    private int numLinks = 0;
    /** The queue of URLs the crawler has still process. */
    private Queue<String> queue = new LinkedList<>();
    /** A Set of all URLs the crawler has already encountered. */
    private HashSet<String> knownURL = new HashSet<>();
    /** The Seed-URLs, starting point for the crawling process. */
    private String[] seedURLs = {};
    /** The crawled Pages, already split in title, header and content. */
    private List<Page> crawledPages = new ArrayList<>();
    /*
     * Initializes the Crawler
     * @param seedURLs seedURLs should containt the absolute URLs, where the
     * crawler starts his crawling.
     */
    Crawler(final String[] seedURLs) {
        this.seedURLs = seedURLs;
        for (String current : seedURLs) {
            queue.add(current);
            knownURL.add(current);
        }
    }

    /**
     * Crawls the provided net starting from the Seed-URLs.
     * Directly parses the files and stores the pages.
     * @return  the number of found sites.
     */
    protected int crawl() {
        long startTimestamp = System.currentTimeMillis();
        Document doc;
        String curURL;

        //crawl every website following links
        while (!queue.isEmpty()) {
            curURL = queue.poll();
            try {
                doc = Jsoup.connect(curURL).get();
                knownURL.add(curURL);
                Page curSite = (Parser.parse(curURL, doc));
                crawledPages.add(curSite);

                //add all new found links into queue
                for (String curLink : curSite.getLinks()) {
                //knownURL is a hashSet, knownURL.add(curLink) is only true, if
                //curLink was added successfully, therefor is a new URL.
                    if (knownURL.add(curLink)) {
                     queue.add(curLink);
                    }
                    numLinks++;
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to fetch: {}", curURL);
            }
            if (crawledPages.size() % 100 == 0) {
                LOGGER.info("Crawled {} pages.", crawledPages.size());
            }
        }
        //Document curSite = Jsoup.connect(null)
        long stopTimeStamp = System.currentTimeMillis();
        long crawlTime = stopTimeStamp - startTimestamp;
        LOGGER.info("Crawling and lemmatizing took: {} ms", crawlTime);
        return crawledPages.size();
    }

    /**
     * Returns the number of websites the crawler has found.
     * @return The number of websites the crawer has found.
     */
    protected int getNumPagesCrawled() {
        return crawledPages.size();
    }

    /**
     * Returns the number of links the crawler has found.
     * @return The number of links the crawer has found.
     */
    protected int getNumLinks() {
        return numLinks;
    }
    protected List<Page> getCrawledPages() {
        return crawledPages;
    }
    protected String[] getSeedURLs() {
        return seedURLs;
    }
}
