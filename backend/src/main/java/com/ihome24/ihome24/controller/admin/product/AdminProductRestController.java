package com.ihome24.ihome24.controller.admin.product;

import com.ihome24.ihome24.dto.request.product.ProductRequest;
import com.ihome24.ihome24.dto.request.product.ProductSeoPatchRequest;
import com.ihome24.ihome24.dto.response.product.ProductListResponse;
import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.dto.response.product.ProductSeoPatchResponse;
import com.ihome24.ihome24.dto.response.product.ProductSeoGenerateAllResult;
import com.ihome24.ihome24.dto.response.product.ProductSeoImportResult;
import com.ihome24.ihome24.dto.response.product.ProductSlugValidationResponse;
import com.ihome24.ihome24.service.product.ProductSeoBulkService;
import com.ihome24.ihome24.service.product.ProductSeoImportService;
import com.ihome24.ihome24.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminProductRestController {

    private final ProductService productService;
    private final ProductSeoImportService productSeoImportService;
    private final ProductSeoBulkService productSeoBulkService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean stock,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy) {
        ProductListResponse response = productService.getProductsPaginated(
                q, categoryId, category, stock, status, page, itemsPerPage, sortBy, orderBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * B-3: обновить только SEO-поля товара.
     */
    @PatchMapping(value = "/{id}/seo", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ProductSeoPatchResponse> patchProductSeo(
            @PathVariable Long id,
            @Valid @RequestBody ProductSeoPatchRequest request) {
        return ResponseEntity.ok(productService.patchProductSeo(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Массовое обновление SEO из CSV (legacy: sku или id).
     */
    @PostMapping(value = "/seo/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductSeoImportResult> importSeoCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productSeoImportService.importCsv(file));
    }

    /** B-5: автогенерация slug, meta_title, meta_description для товаров с пустыми полями. */
    @PostMapping(value = "/seo/generate-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductSeoGenerateAllResult> generateAllProductSeo() {
        return ResponseEntity.ok(productSeoBulkService.generateAll());
    }

    /** B-5: экспорт SEO всех товаров (CSV: id,slug,meta_title,meta_description). */
    @GetMapping(value = "/seo/export-csv", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<byte[]> exportSeoCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"products-seo.csv\"")
                .body(productSeoBulkService.exportCsv());
    }

    /** B-5: импорт SEO из CSV с валидацией строк и отчётом об ошибках. */
    @PostMapping(value = "/seo/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductSeoImportResult> importSeoCsvB5(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productSeoBulkService.importCsv(file));
    }

    /** §5: проверка уникальности slug перед сохранением. */
    @GetMapping(value = "/seo/validate-slug", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductSlugValidationResponse> validateProductSlug(
            @RequestParam String slug,
            @RequestParam(required = false) Long excludeProductId) {
        return ResponseEntity.ok(productService.validateProductSlug(slug, excludeProductId));
    }

    @GetMapping("/next-sku")
    public ResponseEntity<java.util.Map<String, String>> getNextSku() {
        String nextSku = productService.generateNextSku();
        return ResponseEntity.ok(java.util.Collections.singletonMap("sku", nextSku));
    }

    /**
     * Частичное обновление цены варианта по SKU.
     */
    @PatchMapping("/{id}/variant/price")
    public ResponseEntity<ProductResponse> updateVariantPrice(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> payload
    ) {
        String sku = payload != null ? (String) payload.get("sku") : null;
        Object priceObj = payload != null ? payload.get("price") : null;
        java.math.BigDecimal price = priceObj != null ? new java.math.BigDecimal(priceObj.toString()) : null;
        ProductResponse response = productService.updateVariantPrice(id, sku, price);
        return ResponseEntity.ok(response);
    }

    /**
     * Частичное обновление количества на складе варианта по SKU.
     */
    @PatchMapping("/{id}/variant/stock")
    public ResponseEntity<ProductResponse> updateVariantStock(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> payload
    ) {
        String sku = payload != null ? (String) payload.get("sku") : null;
        Object qtyObj = payload != null ? payload.get("stockQuantity") : null;
        Integer stockQuantity = null;
        if (qtyObj != null) {
            stockQuantity = Integer.parseInt(qtyObj.toString());
        }
        ProductResponse response = productService.updateVariantStock(id, sku, stockQuantity);
        return ResponseEntity.ok(response);
    }
}
