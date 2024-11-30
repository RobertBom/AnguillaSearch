package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Crawler Class of the AnguillaSearch project.
 * Crawls the specified net and extracts the titles, header and 
 * content of the pages.
 * The net can be specified by a path to a json with the field "Seed-URLs"
 * or by providing a String array with the Seed-URLs directly.
 */
public class Crawler {
    /** The path to the Json the crawler was initialized. */
    private String netJsonPath = "";
    /** The Number of Pages the crawler has crawled. */
    private int numPagesCrawled = 0;
    /** The Number of Links the crawler has found. */
    private int numLinks = 0;
    /** The queue of URLs the crawler has still process. */
    private Queue<String> queue = new LinkedList<>();
    /** A Set of all URLs the crawler has already encountered. */
    private HashSet<String> knownURL = new HashSet<>();
    /** The Seed-URLs, starting point for the crawling process. */
    private String[] seedURLs;
    /** The crawled Pages, already split in title, header and content. */
    private List<Page> crawledPages = new ArrayList<>();
    /** private ArrayList<String> seedURLs = new ArrayList<>(); */

    /*
     * Initializes the Crawler, reads the provided Json and parses the 
     * "seed-URLs" field. Uses the provided information as a starting point
     * for the crawling process.
     * @param netJsonPath netJsonPath a path to a json which contains the 
     * Seed-URLs for the crawler.
     */
    Crawler(final String netJsonPath) throws IOException {
        this.netJsonPath = netJsonPath;
        readNetJSON(netJsonPath);
        if (seedURLs.length == 0) {
            System.out.println("No SeedURLs provided by the Json.");
        }
        //add all seedURLs to the queue
        for (String seedURL : seedURLs) {
            queue.add(seedURL);
        }
    }

    /*
     * Initializes the Crawler
     * @param seedURLs seedURLs should containt the absolute URLs, where the
     * crawler starts his crawling.
     */
    Crawler(final String[] seedURLs) {
        for (String current : seedURLs) {
            queue.add(current);
            knownURL.add(current);
        }
    }

    /**
     * Reads the Json file, provided by the file path and reads out the Seed-URLs for the crawler and saves it in the attribute seedURLs.
     * @param   netJsonPath a path to a json which contains the Seed-URLs for the crawler.
     */
    private void readNetJSON(final String netJsonPath) throws IOException {
        JsonObject json = Utils.parseJSONFile(netJsonPath);
        seedURLs = new Gson().fromJson(json.get("Seed-URLs"), String[].class);
    }

    /**
     * Crawls the provided net starting from the Seed-URLs.
     * Directly parses the files and stores the pages.
     * @return  the number of found sites.
     */
    protected int crawl() {
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
                numPagesCrawled++;

                //add all new found links into queue
                for (String curLink : curSite.getLinks()) {
                //knownURL is a hashSet, knownURL.add(curLink) is only true, if
                //curLink was added successfully, therefor is a new URL.
                    if (knownURL.add(curLink)) {
                     queue.add(curLink);
                    }
                numLinks++; 
                }
            }
            catch (IOException e) {
                System.out.println("Failed to fetch: " + curURL);
            }   

            //System.out.println(numPagesCrawled + "Pages crawled, Queue Size:" + queue.size());

        }
        //Document curSite = Jsoup.connect(null)
        return numPagesCrawled;
    }

    /**
     * Returns the number of websites the crawler has found.
     * @return The number of websites the crawer has found.
     */
    public int getNumPagesCrawled() {
        return numPagesCrawled;
    }

    /**
     * Returns the number of links the crawler has found.
     * @return The number of links the crawer has found.
     */
    public int getNumLinks() {
        return numLinks;
    }

    public List<Page> getCrawledPages() {
        return crawledPages;
    }



}
