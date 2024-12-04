package de.fernunihagen.dbis.records;

/**
 * Simple record to have a pair of a token and it's IDF 
 * (Inverted Document Frequency) value.
 * @param token the token
 * @param idf the IDF (Inverse Document Frequency) value for that token
 */
public record TokenIDF(
    String token,
    double idf
) { }
