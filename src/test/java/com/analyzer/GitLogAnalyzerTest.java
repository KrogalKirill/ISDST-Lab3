package com.analyzer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

class GitLogAnalyzerTest {

    private final String sampleLog = """
        a1b2c3d|Alice|Initialization: project setup
        e4f5g6h|Bob|Fill: Commit model
        i7j8k9l|Alice|Hotfix: urgent patch
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
        props.setProperty("git.search.keywords", "Initialization,Fill,Hotfix");
        GitLogAnalyzer analyzer = new GitLogAnalyzer(sampleLog, props);

        var found = analyzer.findCommitsWithKeywords();
        assertEquals(3, found.size()); // Все 3 коммита содержат ключевые слова
        assertTrue(found.stream().anyMatch(c -> c.getHash().equals("a1b2c3d")));
        assertTrue(found.stream().anyMatch(c -> c.getHash().equals("e4f5g6h")));
        assertTrue(found.stream().anyMatch(c -> c.getHash().equals("i7j8k9l")));
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