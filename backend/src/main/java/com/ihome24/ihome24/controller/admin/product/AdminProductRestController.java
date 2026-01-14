package com.ihome24.ihome24.controller.admin.product;

import com.ihome24.ihome24.dto.request.product.ProductRequest;
import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/products", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminProductRestController {

    private final ProductService productService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                       @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }
}
