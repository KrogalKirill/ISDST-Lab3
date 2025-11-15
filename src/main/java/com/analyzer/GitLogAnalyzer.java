package com.analyzer;

import com.analyzer.model.Commit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GitLogAnalyzer {

    private final List<Commit> commits;
    private final List<String> keywords;
    private final String outputFormat;
    private final String outputFile;

    public GitLogAnalyzer(String gitLogOutput, Properties properties) {
        this.commits = parseGitLog(gitLogOutput);
        this.keywords = Arrays.stream(properties.getProperty("git.search.keywords", "")
                        .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        this.outputFormat = properties.getProperty("output.format", "JSON").trim().toUpperCase();
        this.outputFile = properties.getProperty("output.file", "git-report.json").trim();
    }

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

    public List<Map.Entry<String, Long>> getTopAuthors(int limit) {
        return commits.stream()
                .collect(Collectors.groupingBy(Commit::getAuthor, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Commit> findCommitsWithKeywords() {
        return commits.stream()
                .filter(c -> keywords.stream()
                        .anyMatch(kw -> c.getMessage().toUpperCase().contains(kw.toUpperCase())))
                .collect(Collectors.toList());
    }

    public List<String> getAllAuthorsSorted() {
        return commits.stream()
                .map(Commit::getAuthor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private String generateJsonReport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ArrayNode topAuthors = mapper.createArrayNode();
        getTopAuthors(3).forEach(entry -> {
            ObjectNode node = mapper.createObjectNode();
            node.put("name", entry.getKey());
            node.put("commits", entry.getValue());
            topAuthors.add(node);
        });
        root.set("top_authors", topAuthors);

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

    private void printPlainTextReport() {
        System.out.println("=== АНАЛИЗ GIT ЛОГА ===");
        System.out.println();

        System.out.println("ТОП-3 АВТОРОВ:");
        List<Map.Entry<String, Long>> top = getTopAuthors(3);
        if (top.isEmpty()) {
            System.out.println("  Нет коммитов.");
        } else {
            for (int i = 0; i < top.size(); i++) {
                var e = top.get(i);
                System.out.printf("  %d. %s — %d коммит(ов)%n", i + 1, e.getKey(), e.getValue());
            }
        }
        System.out.println();

        System.out.println("КОММИТЫ С КЛЮЧЕВЫМИ СЛОВАМИ:");
        List<Commit> found = findCommitsWithKeywords();
        if (found.isEmpty()) {
            System.out.println("  Не найдено.");
        } else {
            found.forEach(c -> System.out.printf("  [%s] %s%n", c.getHash().substring(0, 7), c.getMessage()));
        }
        System.out.println();

        System.out.println("ВСЕ АВТОРЫ (по алфавиту):");
        List<String> authors = getAllAuthorsSorted();
        if (authors.isEmpty()) {
            System.out.println("  Нет авторов.");
        } else {
            authors.forEach(a -> System.out.println("  • " + a));
        }
    }

    public void run() throws Exception {
        if ("PLAINTEXT".equals(outputFormat)) {
            printPlainTextReport();
        } else {
            String json = generateJsonReport();
            System.out.println(json);

            Path outputPath = Paths.get(outputFile);
            Path parentDir = outputPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir); // ← Безопасно: только если есть родитель
            }
            Files.writeString(outputPath, json, StandardCharsets.UTF_8);

            System.out.println("\nJSON-отчёт сохранён в: " + outputPath.toAbsolutePath());
        }
    }
}