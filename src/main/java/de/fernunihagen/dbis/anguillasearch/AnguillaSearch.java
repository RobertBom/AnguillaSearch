package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


/**
 * Main class of the AnguillaSearch project.
 */
public final class AnguillaSearch {
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
         * Set the java.awt.headless property to true to prevent awt from 
         * opening windows.
         * If the property is not set to true, the program will throw an 
         * exception when trying to generate the graph visualizations in a 
         * headless environment.
         */
        System.setProperty("java.awt.headless", "true");
        String[] seedURLs = null;
        String jsonPath = null;
        int rankMode = 2;

        Map<String, String> argMap = argsParser(args);
        if (argMap.get("-r") != null) {
            rankMode = Integer.parseInt(argMap.get("-r"));
        }
        if (argMap.get("seedURLs") != null) {
            seedURLs = argMap.get("seedURLs").split(" ");
        } else if (argMap.get("jsonPath") != null) {
            jsonPath = argMap.get("jsonPath");
            try {
                JsonObject json = Utils.parseJSONFile(jsonPath);
                seedURLs = new Gson().fromJson(json.get("Seed-URLs"), 
                                      String[].class);

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
            System.out.println("No seedURLs or json provided using default"
                               + " seedURLs: " + String.join(", ", seedURLs));

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
                System.out.println("Combination of Cosine Similarity and"
                                   + " Pagerank.");
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
    public static Map<String, String> argsParser(final String[] args) 
                                      throws IllegalArgumentException {
        Map<String, String> argMap = new HashMap<>();
        int argCount = args.length;
        // every argument support at maximum one parameter so we can use bool.
        Map<String, Boolean> validArgs = new HashMap<>();
        validArgs.put("-r", true);
        validArgs.put("-color", false);

        if (argCount == 0) {
            return argMap;
        }
        int i = 0;
        // process all arguments of type -arg and -arg parameter.
        while (i < argCount) {
            if (validArgs.containsKey(args[i])) {
                String cArg = args[i];
                if (validArgs.get(cArg)) {
                    if (args[i + 1].charAt(0) == '-') {
                        invalidArguments(args[i + 1]);
                    } else {
                        String cOpt = args[i + 1];
                        argMap.put(cArg, cOpt);
                        i++;
                    }
                } else {
                    argMap.put(cArg, "");
                }
            // the args we are reading is not in our validArgs map, but might
            // out last argument, which is the target json or seed urls.
            } else if (i == argCount - 1) {
                if (args[argCount - 1].contains("http://") 
                    || args[argCount - 1].contains("https://")) {
                    
                    argMap.put("seedURLs", args[argCount - 1]);
                // here we assume that 
                } else if (args[argCount - 1].charAt(0) != '-') {
                    argMap.put("jsonPath", args[argCount - 1]);
                }
            } else {
                invalidArguments(args[i]);
            }
            i++;
        }
        // check if the parameter for argument -r is in range.
        if (argMap.containsKey("-r")) {
            if (!argMap.get("-r").matches("^[012]$")) {
                System.out.println("-r option only accepts 0, 1 or 2 as" 
                                    + " parameter.");
                System.out.println("0 - TF-IDF ranking.");
                System.out.println("1 - Cosine similarity ranking.");
                System.out.println("2 - Combination of TF-IDF and cosine "
                                    + "similarity.");
                System.exit(2);
            }
        }
        return argMap;
    }
    private static void printQuery(final Indexer index, final String query, 
                                   final int rankMode) {
        List<SearchResult> searchResults = index.searchQuery(query, rankMode);
        List<String> queryLemmas = Parser.tokLem(query);
        int i = 1;
        if (searchResults.isEmpty()) {
            System.out.format("No search results found for query \"%s\"%n%n",
                              query);
        }
        for (SearchResult searchResult : searchResults) {
            System.out.format("Result %d:%n", i);
            System.out.format("URL: %s%n", searchResult.url());
            String title = searchResult.page().getTitle();
            String sentenceWithQuery = getFirstQuerySentence(searchResult.
                                                            page(), query);
            String header = searchResult.page().getHeader();
            if (color) {
                title = highlightString(title, queryLemmas);
                sentenceWithQuery = highlightString(sentenceWithQuery,
                                                    queryLemmas);
                header = highlightString(header, queryLemmas);
            }
            System.out.format("Title: \"%s\"%n", title);
            System.out.format("Headings: \"%s\"%n", header);
            System.out.format("Content: \"%s\"", sentenceWithQuery);
            System.out.format("%n%n");
            i++;
        }
    }
    private static String getFirstQuerySentence(final Page p, 
                                                final String query) {
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
        if (!matchedLemmaList.isEmpty() && color) {
            firstSentence = highlightString(firstSentence, matchedLemmaList);
        }
        return firstSentence;
    }
    private static String highlightString(final String inputString,
                                    final List<String> matchesToHighlight) {
        String outString = inputString;
        List<String> allCaseMatchList = new ArrayList<>(matchesToHighlight);
        /* We add lowercase or uppercase versions to matchesToHighlight so 
         * don't change the case later on */
        for (String match : matchesToHighlight) {
            char firstC = match.charAt(0);
            if (Character.isLowerCase(firstC)) {
                String upperCaseV = Character.toUpperCase(firstC)
                                    + match.substring(1);
                allCaseMatchList.add(upperCaseV);
            // we still check since not every character has a case and we do
            // not want to duplicate entries.
            } else if (Character.isUpperCase(firstC)) {
                String lowerCaseV = Character.toLowerCase(firstC) 
                                    + match.substring(1);
                allCaseMatchList.add(lowerCaseV);
            }
        }
        for (String match : allCaseMatchList) {
            outString = outString.replaceAll(match, "\u001B[32m" + match 
                                             + "\u001B[0m");
        }
        return outString;
    }
    private static void invalidArguments(final String unrecognized) {
        System.out.println("Unrecognized option: " + unrecognized);
        System.out.println("Try 'java -jar AnguillaSearch.java --help'");
        System.exit(1);
    }
    private static void printHelp() {

    }
}
