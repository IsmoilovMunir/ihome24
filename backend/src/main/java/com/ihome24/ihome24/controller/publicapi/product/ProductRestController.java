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
        // Только активные товары в наличии (stockQuantity == null или > 0)
        List<ProductResponse> products = productService.getActiveProductsInStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id, true);
        if (product.getIsActive() == null || !product.getIsActive()) {
            return ResponseEntity.notFound().build();
        }
        // Не показывать товары не в наличии (как в каталоге)
        if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
}
