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

public class AufgabeZ1 {
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

        //generate urlMap
        for (Page p : crawledPages) {
            urlMap.put(p.getURL(), p);
        }

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
        Object[] vertices = new Object[16];

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
                vertices[i] = graph.insertVertex(parent,
                                                 cUrl,
                                                 cUrl,
                                                 20+40*i,
                                                 20+20*i,
                                                 250,
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
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 2, Color.WHITE, true, null);
		File imgFile = new File("figures/net-graph.png");
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
