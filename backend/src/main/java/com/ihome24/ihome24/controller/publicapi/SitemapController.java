package com.ihome24.ihome24.controller.publicapi;

import com.ihome24.ihome24.service.seo.SitemapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Публичные sitemap для поисковиков (без префикса /api).
 * Кеш ответа: 6 часов ({@link #CACHE_MAX_AGE_SECONDS}).
 */
@RestController
@RequiredArgsConstructor
public class SitemapController {

    /** 6 часов — по требованиям SEO-спринта */
    static final int CACHE_MAX_AGE_SECONDS = 21_600;

    private static final String CACHE_CONTROL = "public, max-age=" + CACHE_MAX_AGE_SECONDS;

    private final SitemapService sitemapService;

    /**
     * Основной sitemap: статические страницы, все активные категории и товары.
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        return xmlResponse(sitemapService.generateSitemapXml());
    }

    @GetMapping(value = "/sitemap-static.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapStatic() {
        return xmlResponse(sitemapService.generateStaticSitemapXml());
    }

    @GetMapping(value = "/sitemap-categories.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapCategories() {
        return xmlResponse(sitemapService.generateCategoriesSitemapXml());
    }

    @GetMapping(value = "/sitemap-products.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapProducts() {
        return xmlResponse(sitemapService.generateProductsSitemapXml());
    }

    private static ResponseEntity<String> xmlResponse(String xml) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL)
                .body(xml);
    }
}
