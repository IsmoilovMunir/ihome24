package com.ihome24.ihome24.controller.publicapi;

import com.ihome24.ihome24.service.seo.SitemapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeoController {

    private final SitemapService sitemapService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapIndex() {
        String xml = sitemapService.generateSitemapIndexXml();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .body(xml);
    }

    @GetMapping(value = "/sitemap-static.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapStatic() {
        String xml = sitemapService.generateStaticSitemapXml();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .body(xml);
    }

    @GetMapping(value = "/sitemap-categories.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapCategories() {
        String xml = sitemapService.generateCategoriesSitemapXml();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .body(xml);
    }

    @GetMapping(value = "/sitemap-products.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemapProducts() {
        String xml = sitemapService.generateProductsSitemapXml();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                .body(xml);
    }
}
