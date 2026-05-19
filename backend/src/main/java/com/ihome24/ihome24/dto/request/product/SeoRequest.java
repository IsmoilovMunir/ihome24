package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SeoRequest {
    private String slug;

    @Size(max = 120, message = "Meta Title не должен превышать 120 символов")
    private String metaTitle;

    @Size(max = 300, message = "Meta Description не должен превышать 300 символов")
    private String metaDescription;
}
