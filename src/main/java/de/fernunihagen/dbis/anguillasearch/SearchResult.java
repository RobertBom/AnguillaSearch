package de.fernunihagen.dbis.anguillasearch;

public record SearchResult(
    String url,
    Page page,
    double score
) { }
