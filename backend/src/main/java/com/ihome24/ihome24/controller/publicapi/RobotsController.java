package com.ihome24.ihome24.controller.publicapi;

import com.ihome24.ihome24.service.seo.RobotsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Публичный robots.txt (без префикса /api).
 */
@RestController
@RequiredArgsConstructor
public class RobotsController {

    private static final String CACHE_CONTROL = "public, max-age=" + SitemapController.CACHE_MAX_AGE_SECONDS;

    private final RobotsService robotsService;

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL)
                .body(robotsService.generateRobotsTxt());
    }
}
