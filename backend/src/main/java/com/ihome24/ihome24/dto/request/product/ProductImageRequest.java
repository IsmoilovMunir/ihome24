package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductImageRequest {
    @NotBlank(message = "URL изображения обязателен")
    @Size(max = 500, message = "URL изображения не должен превышать 500 символов")
    private String imageUrl;

    @Size(max = 255, message = "Альтернативный текст не должен превышать 255 символов")
    private String altText;

    private Integer sortOrder = 0;
    private Boolean isPrimary = false;
}
