package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


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

            JsonObject json = Utils.parseJSONFile("intranet/cheesy3-7fdaa098.json");
            String[] seedURLs = new Gson().fromJson(json.get("Seed-URLs"), String[].class);

            Indexer index = new Indexer(seedURLs);
            index.printInfo();

            String searchString = "burrata slovakianbryndza cantal";
            System.out.println("TF-IDF:");
            List<SearchResult> sr = index.searchQuery(searchString,0);
            index.printSearchResults(sr);
            System.out.println("Cosine Similarity:");
            sr = index.searchQuery(searchString, 1);
            index.printSearchResults(sr);
            System.out.println("Pagerank Cosine Combination:");
            sr = index.searchQuery(searchString, 2);
            index.printSearchResults(sr);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
    public Map<String, List<String>> argsParser(final String[] args) {
        Map<String, List<String>> argMap = new HashMap<>();
        return argMap;
    }
}
