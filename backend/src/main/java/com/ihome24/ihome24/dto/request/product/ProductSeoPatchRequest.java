package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * B-3: частичное обновление SEO товара из админки.
 * {@code null} — поле не менять; пустая строка — сбросить (для title/description — автогенерация при чтении).
 */
@Data
public class ProductSeoPatchRequest {

    @Size(max = 100, message = "Slug не должен превышать 100 символов")
    private String slug;

    @Size(max = 120, message = "Meta Title не должен превышать 120 символов")
    private String metaTitle;

    @Size(max = 300, message = "Meta Description не должен превышать 300 символов")
    private String metaDescription;
}
