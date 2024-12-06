package de.fernunihagen.dbis.anguillasearch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * Unit tests for the reverse index.
 */
class ReverseIndexTests {

    static List<JsonObject> testPages;
    static JsonObject correctReverseIdex;
    static List<Page> pageList = new LinkedList<>();
    static Indexer index;


    @BeforeAll
    static void setUp() throws IOException {

        testPages = Utils.parseAllJSONFiles(java.util.Optional.of("src/test/resources/tf-idf/pages"));
        correctReverseIdex = Utils.parseJSONFile("src/test/resources/tf-idf/index.json");

        // Add your code here to create your reverse index

        // Create Page Objects
        for(JsonObject testPage : testPages) {
            String url = testPage.get("url").getAsString();
            String title = testPage.get("title").getAsString();
            String header = testPage.get("headings").getAsString();
            String content = testPage.get("paragraphs").getAsString();
            Page p = new Page(url, title, header, content, new HashSet<String>());
            pageList.add(p);
        }
        index = new Indexer(pageList);
    }

        


    @Test
    void reverseIdexTFIDF() {

        for (Entry<String, JsonElement> entry : correctReverseIdex.entrySet()) {
            // The token of the reverse index
            String token = entry.getKey();
            JsonObject pagesMap= entry.getValue().getAsJsonObject();
            for (Entry<String, JsonElement> pageEntry : pagesMap.entrySet()) {

                // The URL of the page
                String url = pageEntry.getKey();
                // The TF-IDF value of the token in the page
                Double tfidf = pageEntry.getValue().getAsDouble();


                // Add your code here to compare the TF-IDF values of your reverse index with the correct values

                // Check if the reverse index contains the token
                //replaced "assertTrue( index.getReverseIndexValues(token) != null );" with:
                assertNotNull(index.getReverseIndexValues(token));

                // Get the map of pages for the token
                Map<String, Page> tokenMap = index.getReverseIndexValues(token);

                // Check if the URL exists for that token
                assertTrue(tokenMap.containsKey(url));

                // Get the TF-IDF value for the URL from your reverse index
                Double indexTfidf= index.calcTFIDF(token, tokenMap.get(url));
                // Check if the TF-IDF value is correct
                assertTrue(Math.abs(tfidf - indexTfidf) < 0.0001);

                // Remove the following line after adding your code
            }
        }
    }
}