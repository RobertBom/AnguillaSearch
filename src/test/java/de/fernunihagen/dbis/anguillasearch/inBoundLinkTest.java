package de.fernunihagen.dbis.anguillasearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Test that checks if the data of inbound links.
 */
public class inBoundLinkTest {
    // copy of CrawlerTests.java
    static List<JsonObject> testJSONs = new ArrayList<>();

    @BeforeAll
    static void setUp() throws IOException {
        // Load the metadata from the JSON file
        testJSONs.add(Utils.parseJSONFile("intranet/cheesy1-f126d0d3.json"));
        testJSONs.add(Utils.parseJSONFile("intranet/cheesy2-c79b0581.json"));
        testJSONs.add(Utils.parseJSONFile("intranet/cheesy3-7fdaa098.json"));
        testJSONs.add(Utils.parseJSONFile("intranet/cheesy4-a31d2f0d.json"));
        // Big net testJSONs.add(Utils.parseJSONFile("intranet/cheesy5-d861877d.json"));
        testJSONs.add(Utils.parseJSONFile("intranet/cheesy6-54ae2b2e.json"));
    }

    @Test
    void crawlAllWebsitesInProvidedNetwork() {
        // Iterate over all test JSON files
        for (JsonObject testJSON : testJSONs) {
            // Extract the seed URLs from the JSON file
            String[] seedUrls = new Gson().fromJson(testJSON.get("Seed-URLs"), String[].class);
            
     
            // Crawler crawls pages and PageRank class created inbound links map.
            Crawler curCrawler = new Crawler(seedUrls);
            curCrawler.crawl();
            PageRank pageRank = new PageRank(curCrawler.getCrawledPages());

            // get map of all inbound links and cumulate them
            int countInBound = pageRank.getTotalInBoundLinks();
            
            // Verify that the number of inbound links is correct, i.e.
            // the same as the number of (outbound) links stated in the JSON.
            assertEquals(testJSON.get("Num-Links").getAsInt(), countInBound);

            // Check if every Seed-URL has 0 inbound links as is the specification.
            for(String seedurl : seedUrls) {
                assertEquals(0, pageRank.getNumInboundLinks(seedurl));
            }
        }
    }
    
}
