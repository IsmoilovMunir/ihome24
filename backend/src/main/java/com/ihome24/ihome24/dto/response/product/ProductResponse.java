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
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String sku;
    private Integer stockQuantity;
    private Boolean isActive;
    private Boolean isFeatured;
    private String imageUrl;
    private CategoryResponse category;
    private List<CharacteristicResponse> characteristics = new ArrayList<>();
    private List<ProductImageResponse> images = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
