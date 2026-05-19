package com.ihome24.ihome24.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Загружает переменные из {@code backend/.env} (канонический файл секретов Spring).
 * При запуске из {@code infrastructure/} или корня репозитория ищет {@code …/backend/.env} вверх по дереву.
 * Опционально {@code .env} в текущей папке — только если {@code backend/.env} не найден.
 */
public final class EnvLoader {

    private static Map<String, String> loadedFromFile = null;

    public static void loadEnvIfPresent() {
        if (loadedFromFile != null) {
            return;
        }
        loadedFromFile = new HashMap<>();
        Path envPath = findEnvFile();
        String cwd = System.getProperty("user.dir");
        if (envPath == null || !Files.isReadable(envPath)) {
            System.out.println("[EnvLoader] backend/.env not found (cwd=" + cwd
                    + "). Создайте backend/.env по образцу backend/.env.example");
            loadedFromFile = Collections.unmodifiableMap(loadedFromFile);
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
                int eq = trimmed.indexOf('=');
                if (eq <= 0) continue;
                String key = trimmed.substring(0, eq).trim();
                String value = parseValue(trimmed.substring(eq + 1));
                if (key.isEmpty() || value == null) continue;
                loadedFromFile.put(key, value);
                System.setProperty(key, value);
                count++;
            }
            if (count > 0) {
                System.out.println("[EnvLoader] Loaded " + count + " variables from " + envPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[EnvLoader] Failed to read .env: " + e.getMessage());
        }
        loadedFromFile = Collections.unmodifiableMap(loadedFromFile);
    }

    /** Значение из .env (приоритет над пустой переменной окружения IDE). */
    public static String lookup(String key) {
        loadEnvIfPresent();
        if (key == null) {
            return null;
        }
        String fromFile = loadedFromFile.get(key);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile;
        }
        return null;
    }

    private static String parseValue(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isEmpty()) return "";
        if (raw.startsWith("'") && raw.length() > 1) {
            int end = raw.indexOf('\'', 1);
            if (end != -1) return raw.substring(1, end);
            return raw.substring(1);
        }
        if (raw.startsWith("\"") && raw.length() > 1) {
            int end = raw.indexOf('"', 1);
            if (end != -1) return raw.substring(1, end);
            return raw.substring(1);
        }
        int hash = raw.indexOf('#');
        if (hash != -1) raw = raw.substring(0, hash).trim();
        return raw;
    }

    private static Path findEnvFile() {
        Path cwd = Paths.get(System.getProperty("user.dir")).normalize();
        Path dir = cwd;
        for (int depth = 0; depth < 6 && dir != null; depth++) {
            Path backendEnv = dir.resolve("backend").resolve(".env");
            if (Files.isRegularFile(backendEnv)) {
                return backendEnv;
            }
            dir = dir.getParent();
        }
        Path inCwd = cwd.resolve(".env");
        if (Files.isRegularFile(inCwd)) {
            return inCwd;
        }
        return null;
    }
}
