package com.ihome24.ihome24.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Парсинг и экранирование CSV для массовых SEO-операций.
 */
public final class ProductSeoCsvUtils {

    public static final String EXPORT_HEADER = "id,slug,meta_title,meta_description";

    private ProductSeoCsvUtils() {
    }

    public static List<String> parseLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    public static String escapeField(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static int indexOfHeader(List<String> headers, String... names) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).toLowerCase(Locale.ROOT).trim();
            for (String name : names) {
                if (h.equals(name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String cell(List<String> cols, int idx) {
        if (idx < 0 || idx >= cols.size()) {
            return null;
        }
        String v = cols.get(idx);
        return v != null ? v.trim() : null;
    }

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
