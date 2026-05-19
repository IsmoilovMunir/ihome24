package com.ihome24.ihome24.dto.response.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GET /api/admin/products/seo/validate-slug
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSlugValidationResponse {

    private boolean available;
    private String slug;

    private String error;
    private String message;
    private Long takenByProductId;
}
