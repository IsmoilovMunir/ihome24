package com.ihome24.ihome24.controller.publicapi.product;

import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.dto.response.product.ProductSlugRedirectLookupResponse;
import com.ihome24.ihome24.dto.response.product.ProductSlugResolveResponse;
import com.ihome24.ihome24.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductRestController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getActiveProductsInStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/all-slugs")
    public ResponseEntity<List<String>> getAllSlugs() {
        return ResponseEntity.ok(productService.getAllActiveProductSlugs());
    }

    /**
     * N-2: lookup редиректа для Nitro middleware. newSlug — 301; null — slug канонический; 404 — не найден.
     */
    @GetMapping("/redirect/{slug}")
    public ResponseEntity<ProductSlugRedirectLookupResponse> getProductSlugRedirect(@PathVariable String slug) {
        return productService.lookupSlugRedirect(slug)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * B-2/B-4: товар по slug или id; при устаревшем slug — HTTP 301 на актуальный.
     */
    @GetMapping("/{identifier}")
    public ResponseEntity<ProductSlugResolveResponse> getProduct(@PathVariable String identifier) {
        try {
            ProductSlugResolveResponse result = productService.getProductByIdentifier(identifier, true);
            if (result.isRedirect() && result.getRedirectTargetSlug() != null) {
                String location = "/api/products/" + encodePathSegment(result.getRedirectTargetSlug());
                return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                        .header(HttpHeaders.LOCATION, location)
                        .body(result);
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Deprecated
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductSlugResolveResponse> getProductBySlugLegacy(@PathVariable String slug) {
        return getProduct(slug);
    }

    private static String encodePathSegment(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
