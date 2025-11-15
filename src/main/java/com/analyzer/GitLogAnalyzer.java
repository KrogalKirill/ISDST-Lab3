package com.analyzer;

import com.analyzer.model.Commit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Анализатор логов Git.
 * <p>
 * Выполняет три задачи:
 * <ol>
 *   <li>Топ-3 авторов по количеству коммитов</li>
 *   <li>Поиск коммитов по ключевым словам из app.properties</li>
 *   <li>Список всех уникальных авторов в алфавитном порядке</li>
 * </ol>
 * </p>
 */
public class GitLogAnalyzer {

    private final List<Commit> commits;
    private final List<String> keywords;
    private final String outputFormat;

    /**
     * Создает анализатор.
     *
     * @param gitLogOutput вывод команды {@code git log --pretty=format:"%h|%an|%s"}
     * @param properties   свойства из app.properties
     */
    public GitLogAnalyzer(String gitLogOutput, Properties properties) {
        this.commits = parseGitLog(gitLogOutput);
        this.keywords = Arrays.stream(properties.getProperty("git.search.keywords", "")
                        .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        this.outputFormat = properties.getProperty("output.format", "JSON").toUpperCase();
    }

    /**
     * Парсит вывод git log в список коммитов.
     */
    private List<Commit> parseGitLog(String output) {
        return output.lines()
                .map(line -> {
                    String[] parts = line.split("\\|", 3);
                    if (parts.length != 3) return null;
                    return new Commit(parts[0], parts[1], parts[2]);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Задание 1: Топ-3 авторов.
     */
    public List<Map.Entry<String, Long>> getTopAuthors(int limit) {
        return commits.stream()
                .collect(Collectors.groupingBy(Commit::getAuthor, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Задание 2: Поиск по ключевым словам.
     */
    public List<Commit> findCommitsWithKeywords() {
        return commits.stream()
                .filter(c -> keywords.stream().anyMatch(kw -> c.getMessage().toUpperCase().contains(kw.toUpperCase())))
                .collect(Collectors.toList());
    }

    /**
     * Задание 3: Уникальные авторы в алфавитном порядке.
     */
    public List<String> getAllAuthorsSorted() {
        return commits.stream()
                .map(Commit::getAuthor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Формирует JSON-отчет.
     */
    public String generateJsonReport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        // Top authors
        ArrayNode topAuthors = mapper.createArrayNode();
        getTopAuthors(3).forEach(entry -> {
            ObjectNode node = mapper.createObjectNode();
            node.put("name", entry.getKey());
            node.put("commits", entry.getValue());
            topAuthors.add(node);
        });
        root.set("top_authors", topAuthors);

        // Found keywords
        ArrayNode found = mapper.createArrayNode();
        findCommitsWithKeywords().forEach(c -> {
            ObjectNode node = mapper.createObjectNode();
            node.put("hash", c.getHash());
            node.put("message", c.getMessage());
            found.add(node);
        });
        root.set("found_keywords", found);

        ArrayNode authors = mapper.createArrayNode();
        getAllAuthorsSorted().forEach(authors::add);
        root.set("all_authors", authors);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    /**
     * Запускает анализ и выводит результат.
     */
    public void run() throws Exception {
        String result = generateJsonReport();
        if ("PLAINTEXT".equals(outputFormat)) {
        } else {
            System.out.println(result);
        }
    }
}