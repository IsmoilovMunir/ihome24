package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Название товара обязательно")
    @Size(min = 3, max = 255, message = "Название должно быть от 3 до 255 символов")
    private String name;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    @Digits(integer = 10, fraction = 2, message = "Неверный формат цены")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Старая цена должна быть больше 0")
    @Digits(integer = 10, fraction = 2, message = "Неверный формат старой цены")
    private BigDecimal oldPrice;

    @Size(max = 100, message = "SKU не должен превышать 100 символов")
    private String sku;

    @Min(value = 0, message = "Количество на складе не может быть отрицательным")
    private Integer stockQuantity;

    private Boolean isActive = true;

    private Boolean isFeatured = false;

    @Size(max = 500, message = "URL изображения не должен превышать 500 символов")
    private String imageUrl;

    private Long categoryId;
}
