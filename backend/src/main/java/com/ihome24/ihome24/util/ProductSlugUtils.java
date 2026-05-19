package com.ihome24.ihome24.util;

import java.util.Locale;

/**
 * Разбор legacy URL (числовой id, формат id-slug) для 301-редиректов.
 */
public final class ProductSlugUtils {

    private ProductSlugUtils() {
    }

    public static String normalizeRouteParam(String slugParam) {
        if (slugParam == null) {
            return "";
        }
        return slugParam.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Legacy: только цифры (00001, 45).
     */
    public static Long parseNumericId(String slug) {
        if (slug == null || !slug.matches("\\d+")) {
            return null;
        }
        try {
            return Long.parseLong(slug);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Legacy: {@code 45-kukhonnyi-nozh} → id=45.
     */
    public static Long parseLegacyIdPrefix(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        int dash = slug.indexOf('-');
        if (dash <= 0) {
            return parseNumericId(slug);
        }
        String numPart = slug.substring(0, dash);
        if (!numPart.matches("\\d+")) {
            return null;
        }
        try {
            return Long.parseLong(numPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
