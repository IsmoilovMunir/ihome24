package com.ihome24.ihome24.dto.response.product;

import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    @Builder.Default
    private List<String> benefits = new ArrayList<>();
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String sku;
    private String brand;
    private Integer stockQuantity;
    /** §5.1: наличие на складе (stockQuantity > 0) */
    private Boolean inStock;
    private Integer quantityPerPackage;
    private Boolean isActive;
    private Boolean isFeatured;
    private String imageUrl;
    private CategoryResponse category;
    @Builder.Default
    private List<CharacteristicResponse> characteristics = new ArrayList<>();
    @Builder.Default
    private List<VariantResponse> variants = new ArrayList<>();
    @Builder.Default
    private List<ProductImageResponse> images = new ArrayList<>();

    /** SEO (effective — с автогенерацией при null в БД) */
    private String slug;
    private String metaTitle;
    private String metaDescription;
    private String ogImage;

    private SeoResponse seo;
    private ReturnsResponse returns;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
