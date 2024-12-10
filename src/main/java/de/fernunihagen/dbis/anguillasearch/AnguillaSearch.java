package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


/**
 * Main class of the AnguillaSearch project.
 */
public final class AnguillaSearch {

    //private static final Logger LOGGER = LoggerFactory.getLogger(AnguillaSearch.class);

    private static boolean color = false;
    
    private AnguillaSearch() {
    }

    /**
     * Main method.equ
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {

        // Print start message to logger
        //LOGGER.info("Starting AnguillaSearch...");

        /*
         * Set the java.awt.headless property to true to prevent awt from opening windows.
         * If the property is not set to true, the program will throw an exception when trying to 
         * generate the graph visualizations in a headless environment.
         */
        System.setProperty("java.awt.headless", "true");
        //LOGGER.info("Java awt GraphicsEnvironment headless: {}", java.awt.GraphicsEnvironment.isHeadless());
        String[] seedURLs = null;
        String jsonPath = null;
        int rankMode = 2;


        Map<String, String> argMap = argsParser(args);
        for (var entry: argMap.entrySet()) {
            System.out.format("Key:%-10s\tValue: %s%n", entry.getKey(), entry.getValue());
        }
        if(argMap.get("-r") != null) {
            rankMode = Integer.parseInt(argMap.get("-r"));
        }
        if (argMap.get("seedURLs") != null) {
            seedURLs = argMap.get("seedURLs").split(" ");
        } else if(argMap.get("jsonPath") != null) {
            jsonPath = argMap.get("jsonPath");
            try {
                JsonObject json = Utils.parseJSONFile(jsonPath);
                seedURLs = new Gson().fromJson(json.get("Seed-URLs"), String[].class);

            } catch (IOException e) {
                System.out.println(e.toString());
                System.out.println("Provided json file could not be loaded");
                System.exit(1);
            }
        } else {
            seedURLs = new String[] {"http://creamy-liederkranz24.cheesy1",
            "http://buttery-redleicester.cheesy1",
            "http://rich-jarlsberg.cheesy1"};
            // print out seedURLs
            System.out.println("No seedURLs or json provided using default seedURLs: " + String.join(", ", seedURLs));

        }
        if (argMap.get("-color") != null) {
            color = true;
            System.out.println("Color Outpout loaded");
        }

        Indexer index = new Indexer(seedURLs);
        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.println("You can now start searching.");
        System.out.format("Pages indexed: %d%n", index.getPagesIndexed());
        System.out.print("Searchresults ranked by: ");
        switch (rankMode) {
            case 0:
                System.out.println("TF-IDF.");
                break;
            case 1:
                System.out.println("Cosine Similarity.");
                break;
            case 2:
                System.out.println("Combination of Cosine Similarity and Pagerank.");
                break;
            default:
                System.out.println("Unknown");
                break;
        }
        System.out.println();
        
        // User Input loop, program can be closed with writing exit.
        boolean exit = false;
        while (!exit) {
            System.out.print("Enter search query (or 'exit' to quit): ");
            String userInput = scanner.nextLine();
            if ("exit".equals(userInput)) {
                exit = true;
            } else {
                printQuery(index, userInput, rankMode);
            }
        }
        scanner.close();
    }
    /**
     * Parses arguments for our main program. 
     * The support 4 patterns of arguments:
     * pathToJson
     * List of SeedURls
     * -r Integer pathToJson
     * -r Integer List of SeedURLs
     * The Integer has to be 0, 1 or 2 and selects the ranking method.
     * It returns a map with the possible keys: "-r", "json" "seedURLs" with
     * their corresponding List of Strings.
     * @param args arguments passed with program call
     * @return a map of the command line arguments
     * @throws IllegalArgumentException
     */
    public static Map<String, String> argsParser(final String[] args) throws IllegalArgumentException{
        Map<String, String> argMap = new HashMap<>();
        int argCount = args.length;

        if( argCount == 0) {
            return argMap;
        }
        int i = 0;
        // process all arguments of type -arg and -arg option.
        while (i < argCount) {
            if(args[i].charAt(0) == '-' ) {
                String cArg = args[i];
                // check if next arg exists and does not start with -,
                // then it is the option for that argument.
                if (i+1 < argCount && args[i+1].charAt(0) != '-') {
                    System.out.format("Reading op for: %s%n", cArg);
                    System.out.format("args[i+1] = %s%n", args[i+1]);
                    String cOpt = args[i+1];
                    argMap.put(cArg, cOpt);
                    // the next element in args is already processed so we need to skip it
                    i++;
                } else {
                    // cArg is an Argument without option.
                    argMap.put(cArg, "");
                }
                i++;
            }
        }
        // check if all args have been processed
        if (i < argCount - 1) {
            if(args[argCount -1].contains("http://") || 
            args[argCount -1].contains("https://")) {
                
                argMap.put("seedURLs", args[argCount -1]);
            // here we assume that 
            } else if(args[argCount -1].charAt(0) != '-') {
                argMap.put("jsonPath", args[argCount -1]);
            }
        }
        return argMap;
    }
    private static void printQuery(final Indexer index,final String query, final int rankMode) {
        List<SearchResult> searchResults = index.searchQuery(query, rankMode);
        List<String> queryLemmas = Parser.tokLem(query);
        int i = 1;
        if (searchResults.isEmpty()) {
            System.out.format("No search results found for query \"%s\"%n%n", query);
        }
        for(SearchResult searchResult : searchResults) {
            System.out.format("Result %d:%n", i);
            System.out.format("URL: %s%n", searchResult.url());
            String title = searchResult.page().getTitle();
            String sentenceWithQuery = getFirstQuerySentence(searchResult.page(), query);
            String header = searchResult.page().getHeader();
            if (color) {
                title = highlightString(title, queryLemmas);
                sentenceWithQuery = highlightString(sentenceWithQuery, queryLemmas);
                header = highlightString(header, queryLemmas);
            }
            System.out.format("Title: \"%s\"%n", title);
            System.out.format("Headings: \"%s\"%n", header);
            System.out.format("Content: \"%s\"", sentenceWithQuery);
            System.out.format("%n%n");
            i++;
        }
    }
    private static String getFirstQuerySentence(Page p, String query) {
        // we lemmatize the query to possibly get more matches
        List<String> queryLemmas = Parser.tokLem(query);
        // we split the content into sentences. The punction marks get removed.
        String[] sentences = p.getContent().split("[.!?:;]");
        String firstSentence = null;
        List<String> matchedLemmaList = new ArrayList<>();


        for (int i = 0; i < sentences.length && firstSentence == null; i++) {
            for (String lemma : queryLemmas) {
                if (sentences[i].contains(lemma)) {
                    firstSentence = sentences[i];
                    matchedLemmaList.add(lemma);
                }
            }
        }
        if (firstSentence == null) {
            firstSentence = sentences[0];
        }
        if(!matchedLemmaList.isEmpty() && color) {
            firstSentence = highlightString(firstSentence, matchedLemmaList);
        }
        return firstSentence;
    }
    private static String highlightString(String inputString, List<String> matchesToHighlight) {
        String outString = inputString;
        for(String match : matchesToHighlight) {
            // (?i) to make the match case insenstive.
            outString = outString.replaceAll( "(?i)" + match, "\u001B[32m" + match + "\u001B[0m");
        }
        return outString;
    }
}
