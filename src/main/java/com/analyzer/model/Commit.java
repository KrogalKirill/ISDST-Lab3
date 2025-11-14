package com.analyzer.model;

/**
 * Представляет один коммит из git log.
 */
public class Commit {
    private final String hash;
    private final String author;
    private final String message;

    public Commit(String hash, String author, String message) {
        this.hash = hash;
        this.author = author;
        this.message = message;
    }

    public String getHash() { return hash; }
    public String getAuthor() { return author; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return hash + "|" + author + "|" + message;
    }
}