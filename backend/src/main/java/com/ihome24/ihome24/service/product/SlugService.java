package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.exception.ProductSlugConflictException;
import com.ihome24.ihome24.exception.ProductSlugInvalidException;
import com.ihome24.ihome24.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Генерация и нормализация SEO-slug товара (a-z, 0-9, дефис, макс. 100 символов, уникальность).
 */
@Service
@RequiredArgsConstructor
public class SlugService {

    public static final int MAX_SLUG_LENGTH = 100;
    private static final String SLUG_PATTERN = "^[a-z0-9]+(?:-[a-z0-9]+)*$";

    private final ProductRepository productRepository;

    public String generateSlug(String productName) {
        return generateSlug(productName, null);
    }

    public String generateSlug(String productName, Long excludeProductId) {
        String base = slugify(productName);
        if (base.isBlank()) {
            throw new IllegalArgumentException("Не удалось сформировать slug из названия товара");
        }
        return ensureUnique(base, excludeProductId);
    }

    /**
     * Нормализует slug из админки (транслит + правила) и проверяет уникальность.
     */
    public String normalizeAndValidate(String raw, Long excludeProductId) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String normalized = slugify(raw);
        if (normalized.isBlank() || !normalized.matches(SLUG_PATTERN)) {
            throw new ProductSlugInvalidException("Slug содержит недопустимые символы");
        }
        return ensureUnique(normalized, excludeProductId);
    }

    public boolean isValidSlugFormat(String slug) {
        return slug != null && !slug.isBlank() && slug.length() <= MAX_SLUG_LENGTH && slug.matches(SLUG_PATTERN);
    }

    /**
     * Нормализует slug без автоподбора суффикса (для PATCH из админки).
     */
    public String normalizeWithoutUniqueness(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String normalized = slugify(raw);
        if (normalized.isBlank() || !normalized.matches(SLUG_PATTERN)) {
            throw new ProductSlugInvalidException("Slug содержит недопустимые символы");
        }
        return normalized;
    }

    /**
     * Проверяет уникальность slug; при занятости другим товаром — {@link ProductSlugConflictException}.
     */
    public void assertSlugAvailable(String slug, Long excludeProductId) {
        if (slug == null || slug.isBlank()) {
            return;
        }
        Optional<Product> conflict = findConflictingProduct(slug, excludeProductId);
        if (conflict.isPresent()) {
            Product other = conflict.get();
            throw new ProductSlugConflictException(
                    "Slug уже используется товаром #" + other.getId(),
                    other.getId());
        }
    }

    public Optional<Product> findConflictingProduct(String slug, Long excludeProductId) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        if (excludeProductId == null) {
            return productRepository.findBySlug(slug);
        }
        return productRepository.findBySlug(slug)
                .filter(p -> !excludeProductId.equals(p.getId()));
    }

    private String slugify(String productName) {
        if (productName == null || productName.isBlank()) {
            return "";
        }
        String transliterated = transliterate(productName.trim());
        String lower = transliterated.toLowerCase(Locale.ROOT);
        String slug = lower.replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("^-+|-+$", "");
        if (slug.isBlank()) {
            return "";
        }
        return slug.length() > MAX_SLUG_LENGTH ? slug.substring(0, MAX_SLUG_LENGTH).replaceAll("-+$", "") : slug;
    }

    private String transliterate(String value) {
        Map<Character, String> map = transliterationMap();
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            char lower = Character.toLowerCase(c);
            String mapped = map.get(lower);
            if (mapped != null) {
                out.append(mapped);
            } else if ((lower >= 'a' && lower <= 'z') || (lower >= '0' && lower <= '9')) {
                out.append(lower);
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private String ensureUnique(String base, Long excludeProductId) {
        String candidate = truncateToMax(base);
        if (!exists(candidate, excludeProductId)) {
            return candidate;
        }
        for (int n = 2; n < 10_000; n++) {
            String suffix = "-" + n;
            int maxBase = MAX_SLUG_LENGTH - suffix.length();
            String trimmedBase = base.length() > maxBase ? base.substring(0, maxBase).replaceAll("-+$", "") : base;
            candidate = trimmedBase + suffix;
            if (!exists(candidate, excludeProductId)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Не удалось подобрать уникальный slug");
    }

    private boolean exists(String slug, Long excludeProductId) {
        if (excludeProductId == null) {
            return productRepository.existsBySlug(slug);
        }
        return productRepository.existsBySlugAndIdNot(slug, excludeProductId);
    }

    private String truncateToMax(String slug) {
        if (slug.length() <= MAX_SLUG_LENGTH) {
            return slug;
        }
        return slug.substring(0, MAX_SLUG_LENGTH).replaceAll("-+$", "");
    }

    private Map<Character, String> transliterationMap() {
        Map<Character, String> map = new HashMap<>();
        map.put('а', "a");
        map.put('б', "b");
        map.put('в', "v");
        map.put('г', "g");
        map.put('д', "d");
        map.put('е', "e");
        map.put('ё', "yo");
        map.put('ж', "zh");
        map.put('з', "z");
        map.put('и', "i");
        map.put('й', "y");
        map.put('к', "k");
        map.put('л', "l");
        map.put('м', "m");
        map.put('н', "n");
        map.put('о', "o");
        map.put('п', "p");
        map.put('р', "r");
        map.put('с', "s");
        map.put('т', "t");
        map.put('у', "u");
        map.put('ф', "f");
        map.put('х', "kh");
        map.put('ц', "ts");
        map.put('ч', "ch");
        map.put('ш', "sh");
        map.put('щ', "shch");
        map.put('ъ', "");
        map.put('ы', "y");
        map.put('ь', "");
        map.put('э', "e");
        map.put('ю', "yu");
        map.put('я', "ya");
        return map;
    }
}
