package com.ihome24.ihome24.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {
    private Long id;
    private String imageUrl;
    /** URL оригинала без сжатия (для просмотра на странице товара) */
    private String originalUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean isPrimary;
}
