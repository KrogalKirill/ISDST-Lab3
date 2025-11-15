package com.analyzer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

class GitLogAnalyzerTest {

    private final String sampleLog = """
        a1b2c3d|Alice|FIX: Critical bug in login
        e4f5g6h|Bob|Initial commit
        i7j8k9l|Alice|Update README
        """;

    @Test
    void testTopAuthors() {
        Properties props = new Properties();
        GitLogAnalyzer analyzer = new GitLogAnalyzer(sampleLog, props);

        var top = analyzer.getTopAuthors(3);
        assertEquals(2, top.size());
        assertEquals("Alice", top.get(0).getKey());
        assertEquals(2L, top.get(0).getValue());
        assertEquals("Bob", top.get(1).getKey());
    }

    @Test
    void testKeywordSearch() {
        Properties props = new Properties();
        props.setProperty("git.search.keywords", "FIX:,BUG:");
        GitLogAnalyzer analyzer = new GitLogAnalyzer(sampleLog, props);

        var found = analyzer.findCommitsWithKeywords();
        assertEquals(1, found.size());
        assertEquals("a1b2c3d", found.get(0).getHash());
    }

    @Test
    void testAllAuthorsSorted() {
        Properties props = new Properties();
        GitLogAnalyzer analyzer = new GitLogAnalyzer(sampleLog, props);

        var authors = analyzer.getAllAuthorsSorted();
        assertEquals(2, authors.size());
        assertEquals("Alice", authors.get(0));
        assertEquals("Bob", authors.get(1));
    }
}