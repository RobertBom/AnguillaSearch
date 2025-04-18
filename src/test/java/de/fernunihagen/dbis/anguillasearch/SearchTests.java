package de.fernunihagen.dbis.anguillasearch;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
/**
 * Unit tests for the search.
 */
class SearchTests {

    @Test
    void findCorrectURLs() throws IOException {
        JsonObject testJSON = Utils.parseJSONFile("intranet/cheesy1-f126d0d3.json");
        // Extract the seed URLs from the JSON file
        String[] seedUrls = new Gson().fromJson(testJSON.get("Seed-URLs"), String[].class);
        // Extract the query from the JSON file
        String[] query = new Gson().fromJson(testJSON.get("Query-Token"), String[].class);
        // Extract the expected URLs from the JSON file
        String[] expectedURLs = new Gson().fromJson(testJSON.get("Query-URLs"), String[].class);
        // Execute a search with the given query in the given network via the seed URLs
        List<String> foundURLs; 

        // Place your code here to execute the search
        /* we implemented 3 methods to rank the search results
         * 0 = TF-IDF
         * 1 = Cosine similarity
         * 2 = Combination of PageRank and Cosine similarity.
         * we test all three modes here
         */
        for (int i =0; i <=2; i++) {
            Indexer index = new Indexer(seedUrls);
            // Our search function expects query to be a single String, with the searchterms to be seperated by " "
            String searchQuery = String.join(" ", query);
            // our variable i selects the rankmethod.
            foundURLs = index.search(searchQuery, i);

            // Verify that the found URLs are correct, i.e. the same as stated in the JSON file
            // Uncomment the following line once you have implemented the search
            assertTrue(foundURLs.containsAll(Arrays.asList(expectedURLs)));
        }

        // Remove the following line after adding your code!
        //assertTrue(false);
    }
}