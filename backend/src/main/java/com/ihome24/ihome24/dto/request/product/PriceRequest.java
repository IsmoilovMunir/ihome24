package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceRequest {
    @NotNull(message = "Базовая цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    private BigDecimal base;

    @DecimalMin(value = "0.01", message = "Цена со скидкой должна быть больше 0")
    private BigDecimal sale;

    private String currency = "RUB";

    private Integer vat = 20;
}
