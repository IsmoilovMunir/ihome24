package com.ihome24.ihome24.controller.publicapi.product;

import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductRestController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        // Получаем только активные товары для публичной страницы
        List<ProductResponse> products = productService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        // Проверяем, что товар активен (для публичной страницы)
        if (product.getIsActive() == null || !product.getIsActive()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
}
