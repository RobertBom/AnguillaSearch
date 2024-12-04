package de.fernunihagen.dbis.records;

/**
 * Simple record to have a pair of a token and it's IDF 
 * (Inverted Document Frequency) value.
 * @param token the token
 * @param tfidf the TFIDF value associated with the token
 */
public record TokenTFIDF (
    String token,
    double tfidf
) { }
