package de.fernunihagen.dbis.anguillasearch;

import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
}
