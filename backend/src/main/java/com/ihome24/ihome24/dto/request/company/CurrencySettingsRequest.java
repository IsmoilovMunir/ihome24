package com.ihome24.ihome24.dto.request.company;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrencySettingsRequest {

    @NotNull(message = "Курс валюты обязателен")
    @DecimalMin(value = "0.0001", message = "Курс валюты должен быть больше 0")
    @DecimalMax(value = "99999.9999", message = "Курс валюты не должен превышать 99999.9999")
    private BigDecimal currencyRate;

    @NotNull(message = "Процент коррекции обязателен")
    @DecimalMin(value = "-100", message = "Процент коррекции не должен быть меньше -100")
    @DecimalMax(value = "100", message = "Процент коррекции не должен превышать 100")
    private BigDecimal currencyPercentAdjustment;
}
