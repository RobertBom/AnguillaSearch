package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class of the AnguillaSearch project.
 */
public final class AnguillaSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnguillaSearch.class);
    
    private AnguillaSearch() {
    }

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
        LOGGER.info("Java awt GraphicsEnvironment headless: {}", java.awt.GraphicsEnvironment.isHeadless());
        try {
            String jsonPath = "intranet/cheesy1-f126d0d3.json";
            
            Indexer index = new Indexer(jsonPath);
            index.printInfo();

            List<SearchResult> searchResults = index.searchQuery("ricotta");
            for (SearchResult searchResult : searchResults) {
                System.out.format("URL: %-30s\tTF-IDF: %f%n", searchResult.url(), searchResult.tfidf());
            }
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
