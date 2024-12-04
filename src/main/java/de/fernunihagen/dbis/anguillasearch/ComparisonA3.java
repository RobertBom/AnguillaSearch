package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ComparisonA3 {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnguillaSearch.class);

    /**
     * Main method.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {

        // Print start message to logger
        LOGGER.info("Starting AnguillaSearch...");

        /*
         * Set the java.awt.headless property to true to prevent awt from opening windows.
         * If the property is not set to true, the program will throw an exception when trying to 
         * generate the graph visualizations in a headless environment.
         */
        System.setProperty("java.awt.headless", "true");
        LOGGER.info("Java awt GraphicsEnvironment headless: {}",
        java.awt.GraphicsEnvironment.isHeadless());
        try {

            JsonObject json = Utils.parseJSONFile("intranet/cheesy3-7fdaa098.json");
            String[] seedURLs = new Gson().fromJson(json.get("Seed-URLs"),
            String[].class);

            Indexer index = new Indexer(seedURLs);
            String searchString = "burrata slovakianbryndza cantal";

            List<SearchResult> searchResults = index.searchQuery(searchString, 0);
            for (SearchResult searchResult : searchResults) {
                System.out.format("URL: %-40s\tTFIDF Sum: %f%n", searchResult.url(),
                searchResult.score());
            }

            searchResults = index.searchQuery(searchString, 1);
            for (SearchResult searchResult : searchResults) {
                System.out.format("URL: %-40s\tCosine Similarity: %f%n",
                searchResult.url(), searchResult.score());
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
