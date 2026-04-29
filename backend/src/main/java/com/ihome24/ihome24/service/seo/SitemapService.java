package com.ihome24.ihome24.service.seo;

import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.service.category.CategoryService;
import com.ihome24.ihome24.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SitemapService {

    private static final DateTimeFormatter W3C_DATE = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final ProductService productService;
    private final CategoryService categoryService;

    @Value("${app.seo.site-url:https://ihome24.ru}")
    private String siteUrl;

    public String generateSitemapIndexXml() {
        String baseUrl = normalizeBaseUrl(siteUrl);
        String now = W3C_DATE.format(OffsetDateTime.now(ZoneOffset.UTC));
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                  <sitemap>
                    <loc>%s/sitemap-static.xml</loc>
                    <lastmod>%s</lastmod>
                  </sitemap>
                  <sitemap>
                    <loc>%s/sitemap-categories.xml</loc>
                    <lastmod>%s</lastmod>
                  </sitemap>
                  <sitemap>
                    <loc>%s/sitemap-products.xml</loc>
                    <lastmod>%s</lastmod>
                  </sitemap>
                </sitemapindex>
                """.formatted(baseUrl, now, baseUrl, now, baseUrl, now);
    }

    public String generateStaticSitemapXml() {
        String baseUrl = normalizeBaseUrl(siteUrl);
        List<SitemapUrl> urls = new ArrayList<>();
        addStaticUrls(urls, baseUrl);
        return buildUrlSetXml(urls);
    }

    public String generateCategoriesSitemapXml() {
        String baseUrl = normalizeBaseUrl(siteUrl);
        List<SitemapUrl> urls = new ArrayList<>();
        addCategoryUrls(urls, baseUrl);
        return buildUrlSetXml(urls);
    }

    public String generateProductsSitemapXml() {
        String baseUrl = normalizeBaseUrl(siteUrl);
        List<SitemapUrl> urls = new ArrayList<>();
        addProductUrls(urls, baseUrl);
        return buildUrlSetXml(urls);
    }

    private void addStaticUrls(List<SitemapUrl> urls, String baseUrl) {
        urls.add(new SitemapUrl(baseUrl + "/", null, "daily", "1.0"));
        urls.add(new SitemapUrl(baseUrl + "/products", null, "daily", "0.9"));
        urls.add(new SitemapUrl(baseUrl + "/support/contacts", null, "monthly", "0.6"));
        urls.add(new SitemapUrl(baseUrl + "/support/oferta", null, "monthly", "0.4"));
        urls.add(new SitemapUrl(baseUrl + "/order-tracking", null, "weekly", "0.5"));
        urls.add(new SitemapUrl(baseUrl + "/services", null, "monthly", "0.4"));
    }

    private void addCategoryUrls(List<SitemapUrl> urls, String baseUrl) {
        List<CategoryResponse> categories = categoryService.getAllCategories().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .toList();
        Map<Long, CategoryResponse> byId = new HashMap<>();
        for (CategoryResponse c : categories) {
            byId.put(c.getId(), c);
        }

        for (CategoryResponse category : categories) {
            String path = buildCategoryPath(category, byId);
            if (path == null) continue;
            urls.add(new SitemapUrl(
                    baseUrl + path,
                    toIsoDate(category.getUpdatedAt()),
                    "weekly",
                    "0.8"
            ));
        }
    }

    private void addProductUrls(List<SitemapUrl> urls, String baseUrl) {
        List<ProductResponse> products = productService.getActiveProductsInStock().stream()
                .filter(p -> p.getId() != null)
                .sorted(Comparator.comparing(ProductResponse::getId))
                .toList();
        for (ProductResponse product : products) {
            String slug = slugify(product.getName());
            String path = slug.isEmpty()
                    ? "/products/" + product.getId()
                    : "/products/" + product.getId() + "-" + slug;
            urls.add(new SitemapUrl(
                    baseUrl + path,
                    toIsoDate(product.getUpdatedAt()),
                    "daily",
                    "0.7"
            ));
        }
    }

    private String buildUrlSetXml(List<SitemapUrl> urls) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (SitemapUrl url : urls) {
            xml.append("  <url>\n");
            xml.append("    <loc>").append(escapeXml(url.loc())).append("</loc>\n");
            if (url.lastmod() != null) {
                xml.append("    <lastmod>").append(url.lastmod()).append("</lastmod>\n");
            }
            xml.append("    <changefreq>").append(url.changefreq()).append("</changefreq>\n");
            xml.append("    <priority>").append(url.priority()).append("</priority>\n");
            xml.append("  </url>\n");
        }
        xml.append("</urlset>\n");
        return xml.toString();
    }

    private String buildCategoryPath(CategoryResponse category, Map<Long, CategoryResponse> byId) {
        if (category == null || category.getId() == null) return null;
        List<String> chain = new ArrayList<>();
        CategoryResponse current = category;
        int guard = 0;
        while (current != null && guard < 30) {
            chain.add(0, safeCategorySegment(current));
            Long parentId = current.getParentId();
            current = parentId != null ? byId.get(parentId) : null;
            guard++;
        }
        if (chain.isEmpty()) return null;
        return "/category/" + String.join("/", chain);
    }

    private String safeCategorySegment(CategoryResponse category) {
        String slug = category.getSlug();
        if (slug != null && !slug.isBlank()) return sanitizeSegment(slug);
        String generated = slugify(category.getName());
        if (!generated.isBlank()) return generated;
        return "id-" + category.getId();
    }

    private String sanitizeSegment(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\-_]+", "-");
    }

    private String slugify(String value) {
        if (value == null || value.isBlank()) return "";
        String s = value.toLowerCase(Locale.ROOT).trim();
        Map<Character, String> tr = transliterationMap();
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            String mapped = tr.get(c);
            if (mapped != null) {
                out.append(mapped);
            } else if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                out.append(c);
            } else if (c == ' ' || c == '-' || c == '_') {
                if (out.length() > 0 && out.charAt(out.length() - 1) != '-') {
                    out.append('-');
                }
            }
        }
        String cleaned = out.toString()
                .replaceAll("-{2,}", "-")
                .replaceAll("(^-+|-+$)", "");
        return cleaned.length() > 80 ? cleaned.substring(0, 80) : cleaned;
    }

    private Map<Character, String> transliterationMap() {
        Map<Character, String> map = new HashMap<>();
        map.put('а', "a"); map.put('б', "b"); map.put('в', "v"); map.put('г', "g"); map.put('д', "d");
        map.put('е', "e"); map.put('ё', "e"); map.put('ж', "zh"); map.put('з', "z"); map.put('и', "i");
        map.put('й', "y"); map.put('к', "k"); map.put('л', "l"); map.put('м', "m"); map.put('н', "n");
        map.put('о', "o"); map.put('п', "p"); map.put('р', "r"); map.put('с', "s"); map.put('т', "t");
        map.put('у', "u"); map.put('ф', "f"); map.put('х', "h"); map.put('ц', "ts"); map.put('ч', "ch");
        map.put('ш', "sh"); map.put('щ', "sch"); map.put('ъ', ""); map.put('ы', "y"); map.put('ь', "");
        map.put('э', "e"); map.put('ю', "yu"); map.put('я', "ya");
        return map;
    }

    private String toIsoDate(LocalDateTime dt) {
        if (dt == null) return null;
        OffsetDateTime odt = dt.atOffset(ZoneOffset.UTC);
        return W3C_DATE.format(odt);
    }

    private String normalizeBaseUrl(String raw) {
        String base = Objects.requireNonNullElse(raw, "https://ihome24.ru").trim();
        if (base.endsWith("/")) return base.substring(0, base.length() - 1);
        return base;
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private record SitemapUrl(String loc, String lastmod, String changefreq, String priority) {}
}
