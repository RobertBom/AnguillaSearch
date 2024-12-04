package de.fernunihagen.dbis.anguillasearch;

import java.util.ArrayList;
import java.util.List;

public class Page implements Comparable<Page> {
    /** The URL of the Page saved. */
    private final String url;
    /** The title of the page saved. */
    private String title;
    /** The headings of the page saved. */
    private String header;
    /** The content of the page saved. */
    private String content;
    /** A List of all outgoing Links of the page saved. */
    private List<String> linkList = new ArrayList<>();
    /** A List of all lemmas the page contains. */
    private List<String> filteredLemmaList = new ArrayList<>();


    Page(final String url, final String title, final String header, 
         final String content, final ArrayList<String> linkList) {
        this.url = url;
        this.title = title;
        this.header = header;
        this.content = content;
        this.linkList = linkList;

        //tanks performance
        filteredLemmaList.addAll(Parser.tokLem(title));
        filteredLemmaList.addAll(Parser.tokLem(header));
        filteredLemmaList.addAll(Parser.tokLem(content));
    }

    /**
     * Returns a list of links found on the Website.
     * @return  List of links found on the Website.
     */
    protected List<String> getLinks() {
        return linkList;
    }

    /**
     * Returns the URL of the Page Object.
     * @return the URL of the Page Object
     */
    protected String getURL() {
        return url;
    }

    /** Simple compareTo function which uses the url attribute and
     * String.compareTo()
     * @param p other Page to compare to.
     * @return 0 if the the url attributes of both Pages are the same .
     * < 0 if the url is lexicographically less than the url of p. else > 0 
     */
    @Override
    public int compareTo(final Page p) {
        return this.url.compareTo(p.getURL());
    }

    protected List<String> getFilteredLemmaList() {
        return filteredLemmaList;
    }
    /**
     * Prints all the attributes of the Page.
     */
    public void printPage() {
        System.out.println("URL: " + url);
        System.out.println("Title: " + title);
        System.out.println("Header: " + header);
        System.out.println("Content: " + content);
        System.out.println("Lemma List: ");
        for (String lemma : filteredLemmaList) {
            System.out.print(lemma + ", ");
        }
        System.out.println();
    }

}
