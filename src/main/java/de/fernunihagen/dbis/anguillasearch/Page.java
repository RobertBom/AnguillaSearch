package de.fernunihagen.dbis.anguillasearch;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Page implements Comparable<Page> {
    /** The URL of the Page saved. */
    private final String url;
    /** The title of the page saved. */
    private String title;
    /** The headings of the page saved. */
    private String header;
    /** The content of the page saved. */
    private String content;
    /** A Set of all outgoing Links of the page saved. 
     * A Set because we do not want duplicates */
    private Set<String> linkSet = new HashSet<>();
    /** A List of all lemmas the page contains. */
    private List<String> filteredLemmaList = new LinkedList<>();


    Page(final String url, final String title, final String header, 
         final String content, final Set<String> linkSet) {
        this.url = url;
        this.title = title;
        this.header = header;
        this.content = content;
        this.linkSet = linkSet;

        //tanks performance
        filteredLemmaList.addAll(Parser.tokLem(title));
        filteredLemmaList.addAll(Parser.tokLem(header));
        filteredLemmaList.addAll(Parser.tokLem(content));
    }
    // Constructor without lemmatizing, a lot faster, useful for running tests
    // that do not need lemmatization.
    Page(final String url, final String title, final String header, 
        final String content, final Set<String> linkSet, final boolean noLemma) {
            this.url = url;
            this.title = title;
            this.header = header;
            this.content = content;
            this.linkSet = linkSet;
            
            if (!noLemma) {
                filteredLemmaList.addAll(Parser.tokLem(title));
                filteredLemmaList.addAll(Parser.tokLem(header));
                filteredLemmaList.addAll(Parser.tokLem(content));
            }
    }

    /**
     * Returns a list of links found on the Website.
     * @return  List of links found on the Website.
     */
    public Set<String> getLinks() {
        return (new HashSet<String>(linkSet));
    }
    /**
     * Returns the title of the page.
     * @return the title of the page.
     */
    public String getTitle() {
        return this.title;
    }
    /**
     * Returns the content of the page.
     * @return the content of the page.
     */
    public String getContent() {
        return this.content;
    }
    /**
     * Return the content of the header part of the page.
     * @return the header of the page.
     */
    public String getHeader() {
        return this.header;
    }
    /**
     * Returns the URL of the Page Object.
     * @return the URL of the Page Object.
     */
    protected String getURL() {
        return url;
    }

    /** Simple compareTo function which uses the url attribute and
     *  String.compareTo()
     * @param p other Page to compare to.
     * @return 0 if the the url attributes of both Pages are the same .
     * < 0 if the url is lexicographically less than the url of p. else > 0 
     */
    @Override
    public int compareTo(final Page p) {
        return this.url.compareTo(p.getURL());
    }
    /**
     * Returns a list of all lemmas contained in the page.
     * @return a list of all lemmas the page contains.
     */
    protected List<String> getFilteredLemmaList() {
        return filteredLemmaList;
    }
    /**
     * Removes the specified link from the page.
     * @param link link which should be removed from the pages linkset.
     * @return true if the link was in the page.
     */
    protected boolean removeLink (String link) {
        return linkSet.remove(link);
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
