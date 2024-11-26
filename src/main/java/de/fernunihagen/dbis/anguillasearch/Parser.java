package de.fernunihagen.dbis.anguillasearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Parser Class which provides several functions.
 */
public class Parser {
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
        String content = page.getElementsByTag("main").text();

        Elements links = page.getElementsByTag("a");
        ArrayList<String> linkList = new ArrayList<>();
        for (Element curLink : links) {
            linkList.add(curLink.attr("href"));
        }

        return new Page(url, title, header, content, linkList);
    }

    /**
     * 
     * 
    */
    public static String rmStopWords(final String inputString) {
        /** the String we work on */
        String workString = inputString;
        return workString;
    }

    /**
     * Tokenizes the input String.
     * e.g. 
     * @param content   The String which should be tokenized
     * @return  an ArrayList<String> of the token of content.
     */
    public static ArrayList<String> tokenize(final String inputString) {
        ArrayList<String> outputList = new ArrayList<>();
        
        /** the String which we manipulate to get the tokenized and lemmatized result */
        String workString = inputString;

        //remove StopWords
        workString = rmStopWords(workString);
        //remove punctation marks, 
        workString = workString.replaceAll("[\\.!,]", "");

        //create out StanfordCoreNLP Pipeline with the right annotators
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //process our workstring
        CoreDocument coreDoc = new CoreDocument(workString);
        pipeline.annotate(coreDoc);


        List<CoreLabel> tokenList = coreDoc.tokens();
        for(CoreLabel token : tokenList) {
            outputList.add(token.lemma());
        }

        return outputList;
    }
}
