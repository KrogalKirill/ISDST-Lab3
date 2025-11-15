package com.analyzer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Главный класс приложения.
 */
public class App {
    public static void main(String[] args) {
        try {
            // 1. Читаем app.properties
            Properties props = new Properties();
            File propsFile = new File("app.properties");
            if (propsFile.exists()) {
                try (InputStream is = Files.newInputStream(propsFile.toPath())) {
                    props.load(is);
                }
            }

            // 2. Запускаем git log
            Process process = new ProcessBuilder("git", "log", "--pretty=format:%h|%an|%s")
                    .redirectErrorStream(true)
                    .start();

            String gitOutput = new String(process.getInputStream().readAllBytes());
            process.waitFor();

            // 3. Анализируем
            GitLogAnalyzer analyzer = new GitLogAnalyzer(gitOutput, props);
            analyzer.run();

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}