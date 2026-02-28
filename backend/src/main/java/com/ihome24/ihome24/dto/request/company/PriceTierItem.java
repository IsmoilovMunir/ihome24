package com.ihome24.ihome24.dto.request.company;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Один уровень цен: диапазон количества (minQty–maxQty) и скидка в %.
 * maxQty == null означает «без верхней границы».
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceTierItem {

    @NotNull(message = "Минимальное количество обязательно")
    @Min(value = 0, message = "Минимальное количество не может быть отрицательным")
    private Integer minQty;

    /** null = без верхней границы (и выше) */
    @Min(value = 0, message = "Максимальное количество не может быть отрицательным")
    private Integer maxQty;

    @NotNull(message = "Процент скидки обязателен")
    @DecimalMin(value = "0", message = "Процент скидки не может быть отрицательным")
    @DecimalMax(value = "100", message = "Процент скидки не может превышать 100")
    private BigDecimal discountPercent;

    @NotNull(message = "Название уровня обязательно")
    private String label;
}
