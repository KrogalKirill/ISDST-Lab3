package com.analyzer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        try {
            // 1. Чтение app.properties
            File propsFile = new File("app.properties");
            if (!propsFile.exists()) {
                throw new FileNotFoundException("Файл app.properties не найден!");
            }

            Properties props = new Properties();
            try (InputStream is = new FileInputStream(propsFile)) {
                props.load(is);
            }

            // 2. Запуск git log с UTF-8
            ProcessBuilder pb = new ProcessBuilder(
                    "git", "log", "--pretty=format:%h|%an|%s", "--encoding=UTF-8"
            );
            pb.redirectErrorStream(true);
            pb.environment().put("LANG", "C.UTF-8"); // Принудительно UTF-8

            Process process = pb.start();

            // Читаем напрямую в UTF-8
            String gitOutput = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("git log завершился с ошибкой: " + gitOutput);
            }

            if (gitOutput.trim().isEmpty()) {
                throw new IllegalStateException("Нет коммитов в репозитории.");
            }

            // 3. Анализ
            GitLogAnalyzer analyzer = new GitLogAnalyzer(gitOutput, props);
            analyzer.run();

        } catch (Exception e) {
            System.err.println("ОШИБКА:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}