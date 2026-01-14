package com.ihome24.ihome24.service.product;

import com.ihome24.ihome24.dto.request.product.ProductRequest;
import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.entity.category.Category;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.repository.category.CategoryRepository;
import com.ihome24.ihome24.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Проверка на уникальность SKU, если указан
        if (request.getSku() != null && !request.getSku().isEmpty()) {
            if (productRepository.existsBySku(request.getSku())) {
                throw new IllegalArgumentException("Товар с таким SKU уже существует: " + request.getSku());
            }
        }

        Product.ProductBuilder productBuilder = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .oldPrice(request.getOldPrice())
                .sku(request.getSku())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .imageUrl(request.getImageUrl());

        // Установка категории, если указана
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + request.getCategoryId()));
            productBuilder.category(category);
        }

        Product savedProduct = productRepository.save(productBuilder.build());
        return mapToResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllWithCategory();
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .oldPrice(product.getOldPrice())
                .sku(product.getSku())
                .stockQuantity(product.getStockQuantity())
                .isActive(product.getIsActive())
                .isFeatured(product.getIsFeatured())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        // Добавляем информацию о категории, если она есть
        if (product.getCategory() != null) {
            builder.category(mapCategoryToResponse(product.getCategory()));
        }

        return builder.build();
    }

    private CategoryResponse mapCategoryToResponse(com.ihome24.ihome24.entity.category.Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .isActive(category.getIsActive())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
