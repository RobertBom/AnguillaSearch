package de.fernunihagen.dbis.anguillasearch;

import java.util.ArrayList;
import java.util.List;

public class Page implements Comparable<Page> {
    //The content of a webiste might change, but the url not
    private final String url;
    private String title;
    private String header;
    private String content;
    private List<String> linkList = new ArrayList<>();
    private List<String> filteredLemmaList = new ArrayList<String>();


    Page(String url, String title, String header, String content, ArrayList<String> linkList) {
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
    public List<String> getLinks() {
        return linkList;
    }

    /**
     * 
     * @param p
     * @return
     */
    public String getURL() {
        return url;
    }

    /**
     * Simple compareTo function which uses the url attribute and use String.compareTo()
     * @param p other Page to compare to.
     * @return 0 if the the url attribute is the String. < 0 if the url is lexicographically less than the url of p. else > 0 
     */
    public int compareTo(Page p) {
        return this.url.compareTo(p.getURL());
    }

    public List<String> getFilteredLemmaList() {
        return filteredLemmaList;
    }

    public void printPage() {
        System.out.println("URL: " + url);
        System.out.println("Title: " + title);
        System.out.println("Header: " + header);
        System.out.println("Content: " + content);
        System.out.println("Lemma List: ");
        for(String lemma : filteredLemmaList) {
            System.out.print(lemma + ", ");
        }
        System.out.println();
    }

}
