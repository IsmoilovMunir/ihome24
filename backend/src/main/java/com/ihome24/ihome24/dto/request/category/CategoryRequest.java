package com.ihome24.ihome24.dto.request.category;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Название категории обязательно")
    @Size(min = 2, max = 255, message = "Название должно быть от 2 до 255 символов")
    private String name;

    @NotBlank(message = "Slug обязателен")
    @Size(min = 2, max = 255, message = "Slug должен быть от 2 до 255 символов")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug может содержать только строчные буквы, цифры и дефисы")
    private String slug;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    private Long parentId;

    @Size(max = 500, message = "URL изображения не должен превышать 500 символов")
    private String imageUrl;

    private Boolean isActive = true;

    @Min(value = 0, message = "Порядок сортировки не может быть отрицательным")
    private Integer sortOrder = 0;
}
