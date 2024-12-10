package de.fernunihagen.dbis.anguillasearch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Parser Class which provides several functions.
 */
public abstract class Parser {
    /** variable to save the Regex for Stopword Removal, so we
     *  have to build it only once */
    private static String stopWordRegex = null;
    private static StanfordCoreNLP pipeline = null;

     /**
     * Parses the already fetched page and create a Page-Object, which contains
     * the relevant information.
     * @param   url     the absolute URL of the page.
     * @param   page    the Document-Object, which should be parsed.
     * @return the page as a Page-Class Object. 
     */
    public static Page parse(final String url, final Document page) {

        String title = page.title();
        String header = page.getElementsByTag("header").text(); 

        Elements links = page.getElementsByTag("a");
        Set<String> linkSet = new HashSet<>();
        for (Element curLink : links) {
            linkSet.add(curLink.attr("href"));
        }

        Elements docContent = page.getElementsByTag("main");
        docContent.select("a").remove();
        String content = docContent.text();

        return new Page(url, title, header, content, linkSet);
    }

    /**
     * Removes stopwords of the supplied string. Reads stopwords.txt and
     * expects stopwords to be seperated by \n
     * @param inputString the string of which the stopwords should be removed from
     * @return the inputString, but without stopwords.
    */
    public static String rmStopWords(final String inputString) {
        /** the String we work on */
        String workString = inputString;
        if (stopWordRegex == null) {
            try {
                List<String> stopWords = Files.readAllLines(Paths
                                                        .get("stopwords.txt"));
                //create Regex
                StringBuilder strB = new StringBuilder();
                strB.append("\\b(");
                for (String stopword : stopWords) {
                    strB.append(stopword + "|");
                }
                //remove replace last | by )
                strB.setCharAt(strB.length() -1, ')');
                strB.append("\\b");
                stopWordRegex = strB.toString();
            } catch(IOException e) {
                System.out.println("Failed to load Stopwordlist.");
                System.out.println(e.toString());
            }
        }
        workString = workString.replaceAll(stopWordRegex, "");
        return workString;
    }
    /**
     * Tokenizes and lemmatizes the input String.
     * e.g. 
     * "Welcome to our exquisite selection of artisanal cheeses! Explore the
     * rich flavors and unique textures" will be converted into
     * ["welcome", "to", "we", "exquisite", "selection", "of",
     * "artisanal", "cheese", "explore", "the", "rich", "flavor", "and",
     * "unique", "texture"]
     * @param inputString   The String which should be tokenized
     * @return  an ArrayList<String> of the token of content.
     */
    public static List<String> tokLem(final String inputString) {
        ArrayList<String> outputList = new ArrayList<>();
        
        /** the String which we manipulate to get the tokenized and
         * lemmatized result */
        String workString = inputString;

        // remove \, |, and punctiation marks.
        // – U+2013 : EN DASH not removed
        workString = workString.replaceAll("[\\\\|.!,:\\-?\\&]", "");
        workString = workString.toLowerCase();

        //remove StopWords
        workString = rmStopWords(workString);

        //create out StanfordCoreNLP Pipeline with the right annotators
        if (pipeline == null) {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
            pipeline = new StanfordCoreNLP(props);
        }

        //process our workstring
        CoreDocument coreDoc = new CoreDocument(workString);
        pipeline.annotate(coreDoc);

        //create list of lemmas.
        List<CoreLabel> tokenList = coreDoc.tokens();
        for (CoreLabel token : tokenList) {
            outputList.add(token.lemma());
        }

        return outputList;
    }
}
