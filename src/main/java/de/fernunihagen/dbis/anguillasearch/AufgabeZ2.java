package de.fernunihagen.dbis.anguillasearch;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class AufgabeZ2 {
    static Map<String, Page> urlMap = new HashMap<>();
    
    public static void main(String[] args) {
        String jsonPath = "intranet/cheesy4-a31d2f0d.json";
        String[] seedURLs = new String[0];
        try {
            JsonObject json = Utils.parseJSONFile(jsonPath);
            seedURLs = new Gson().fromJson(json.get("Seed-URLs"), 
                                    String[].class);

        } catch (IOException e) {
            System.out.println(e.toString());
            System.out.println("Provided json file could not be loaded");
            System.exit(1);
        }
        Crawler crawler = new Crawler(seedURLs, 16);
        crawler.crawl();
        List<Page> crawledPages = crawler.getCrawledPages();
        Indexer index = new Indexer(crawledPages);
        index.printInfo();
        cleanLinks(crawledPages);

        // execute search
        String searchQuery = "caerphilly";
        List<SearchResult> searchResults = index.searchQuery(searchQuery, 0);

        //generate urlMap
        for (Page p : crawledPages) {
            urlMap.put(p.getURL(), p);
        }

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
        // set HtmlLabels to true so we can use html formatting in labels.
        graph.setHtmlLabels(true);
        Object[] vertices = new Object[16];

        // set style for default cells
        mxStylesheet styleSheet = graph.getStylesheet();
        Hashtable<String, Object> style = new Hashtable<String, Object>();
        style.put("fillColor", "none");
        style.put("strokeColor", "black");
        style.put("fontColor", "black");
        styleSheet.putCellStyle("default", style);

		graph.getModel().beginUpdate();
		try
		{
            /* When adding the edges we need to get the vertex objects from
             * the url. */
            Map<String, Object> urlVertMap = new HashMap<>();
            // create a Vertex for every page
            for (int i = 0; i < crawledPages.size(); i++) {
                String cUrl = crawledPages.get(i).getURL();
                // get the ranking score current page, default is 0
                double score = 0;
                for (SearchResult sr : searchResults) {
                    if (cUrl.equals(sr.url())) {
                        score = sr.score();
                        break;
                    }
                }
                vertices[i] = graph.insertVertex(parent,
                                                 cUrl,
                                                 "<b>" + cUrl +"</b>" +
                                                  "\n<font color=\"#404040\t\">TF-IDF: " + score + "</font>",
                                                 20+40*i,
                                                 20+20*i,
                                                 270,
                                                 30,
                                                 "default");
                urlVertMap.put(cUrl, vertices[i]);
            }
            // iterate through every page
            for (Page p : crawledPages) {
                String from = p.getURL();
                Set<String> links = p.getLinks();
                Iterator<String> iter = links.iterator();
                // iterater through the links of current page and add edges
                while (iter.hasNext()) {
                    String to = iter.next();
                    graph.insertEdge(parent, null, "", urlVertMap.get(from), urlVertMap.get(to));
                }
            }
            var layout = new mxCircleLayout(graph);
            layout.execute(graph.getDefaultParent());


		}
		finally
		{
			graph.getModel().endUpdate();
		}
        // get height and width of our graph for placing our legend correctly
        double graphHeight = graph.getGraphBounds().getHeight();
        double graphWidth = graph.getGraphBounds().getWidth();
        double legendWidth = 400;
        double legendHeight = 150;
        String legendHTML = "<table><tr><td>net:</td><td>" + jsonPath + 
                            "</td></tr>" +
                            "<tr><td>searchQuery: </td><td>" + searchQuery +
                            "</td></tr></table>";
        try
		{
            graph.insertVertex(parent,
                                "Legend Headline",
                                "<b>Legend</b>",
                                graphWidth -500,
                                graphHeight-200,
                                legendWidth,
                                40,
                                "default;verticalAlign=top;fontSize=30");

            graph.insertVertex(parent,
                                "Legend Content",
                                legendHTML,
                                graphWidth-500,
                                graphHeight-160,
                                legendWidth,
                                100,
                                "default;align=left;verticalAlign=top;fontSize=15");
        }
		finally
		{
			graph.getModel().endUpdate();
		}
;
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 2, Color.WHITE, true, null);
		File imgFile = new File("figures/" + searchQuery +  "-net-graph.png");
		try { 
        	ImageIO.write(image, "PNG", imgFile);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
    }
    private static void cleanLinks (final List<Page> pageList) {
        for (Page p : pageList) {
            Set<String> linkSet = p.getLinks();
            Iterator<String> iter = linkSet.iterator();
            while (iter.hasNext()) {
                String cLink = iter.next();
                if (!containsPage(pageList, cLink)) {
                    p.removeLink(cLink);
                }
            }
        }
    }   
    private static boolean containsPage(final List<Page> pageList, final String url) {
        for (Page p: pageList) {
            if(url.equals(p.getURL())) {
                return true;
            }
        }
        return false;
    }
}
