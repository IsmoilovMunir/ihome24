package com.ihome24.ihome24.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Загружает переменные из backend/.env в System.getProperties(),
 * чтобы Spring подхватил MAIL_USERNAME, MAIL_PASSWORD и др. при любом способе запуска (IDE, mvn, jar).
 */
public final class EnvLoader {

    public static void loadEnvIfPresent() {
        Path envPath = findEnvFile();
        String cwd = System.getProperty("user.dir");
        if (envPath == null || !Files.isReadable(envPath)) {
            System.out.println("[EnvLoader] .env not found (cwd=" + cwd + "). Checked: .env and backend/.env");
            return;
        }
        try {
            List<String> lines = Files.readAllLines(envPath);
            int count = 0;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                // Допускаем пробелы вокруг =
                int eq = trimmed.indexOf('=');
                if (eq <= 0) continue;
                String key = trimmed.substring(0, eq).trim();
                String value = parseValue(trimmed.substring(eq + 1));
                if (key.isEmpty() || value == null) continue;
                // Всегда подставляем из .env (перезаписываем пустые из IDE)
                System.setProperty(key, value);
                count++;
            }
            if (count > 0) {
                System.out.println("[EnvLoader] Loaded " + count + " variables from " + envPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[EnvLoader] Failed to read .env: " + e.getMessage());
        }
    }

    private static String parseValue(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isEmpty()) return "";
        // Одинарные кавычки: '...' — убираем кавычки, не раскрываем переменные
        if (raw.startsWith("'") && raw.length() > 1) {
            int end = raw.indexOf('\'', 1);
            if (end != -1) return raw.substring(1, end);
            return raw.substring(1);
        }
        // Двойные кавычки: "..." — убираем кавычки
        if (raw.startsWith("\"") && raw.length() > 1) {
            int end = raw.indexOf('"', 1);
            if (end != -1) return raw.substring(1, end);
            return raw.substring(1);
        }
        // Без кавычек — обрезаем по # (комментарий)
        int hash = raw.indexOf('#');
        if (hash != -1) raw = raw.substring(0, hash).trim();
        return raw;
    }

    private static Path findEnvFile() {
        // 1) Текущая рабочая директория (для mvn spring-boot:run из backend/)
        Path cwd = Paths.get(System.getProperty("user.dir"));
        Path inCwd = cwd.resolve(".env");
        if (Files.isRegularFile(inCwd)) return inCwd;
        // 2) backend/.env относительно текущей директории
        Path backendEnv = cwd.resolve("backend").resolve(".env");
        if (Files.isRegularFile(backendEnv)) return backendEnv;
        return null;
    }
}
