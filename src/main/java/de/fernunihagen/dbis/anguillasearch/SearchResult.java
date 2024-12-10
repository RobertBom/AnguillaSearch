package de.fernunihagen.dbis.anguillasearch;
/**
 * Simple Record for a search result, that stores the url and the TF-IDF.
 * @param url The URL to store
 * @param tfidf The TF-IDF to store
 */
public record SearchResult(
    String url,
    Page page,
    double score
) { }
