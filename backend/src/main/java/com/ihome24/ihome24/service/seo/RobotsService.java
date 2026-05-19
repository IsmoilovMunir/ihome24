package com.ihome24.ihome24.service.seo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RobotsService {

    @Value("${app.seo.site-url:https://ihome24.ru}")
    private String siteUrl;

    public String generateRobotsTxt() {
        String base = normalizeBaseUrl(siteUrl);
        return """
                User-agent: *
                Allow: /

                Disallow: /api/
                Disallow: /admin/
                Disallow: /cart
                Disallow: /checkout
                Disallow: /account
                Disallow: /personal/
                Disallow: /login/
                Disallow: /register/
                Disallow: /search/

                Sitemap: %s/sitemap.xml
                """.stripIndent().formatted(base);
    }

    private String normalizeBaseUrl(String raw) {
        String base = Objects.requireNonNullElse(raw, "https://ihome24.ru").trim();
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }
}
