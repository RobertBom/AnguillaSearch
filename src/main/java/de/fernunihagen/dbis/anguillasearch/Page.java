package de.fernunihagen.dbis.anguillasearch;

import java.util.ArrayList;

public class Page {
    //The content of a webiste might change, but the url not
    private final String url;
    private String title;
    private String header;
    private String content;
    private ArrayList<String> linkList = new ArrayList<>();


    Page(String url, String title, String header, String content, ArrayList<String> linkList) {
        this.url = url;
        this.title = title;
        this.header = header;
        this.content = content;
        this.linkList = linkList;
    }

    /*
     * Returns a list of links found on the Website.
     * @return  List of links found on the Website.
     */
    public ArrayList<String> getLinks() {
        return linkList;
    }
}
