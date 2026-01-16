package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class LogisticsRequest {
    private BigDecimal weightKg;

    @Valid
    private DimensionsRequest dimensionsCm;

    @Valid
    private DeliveryRequest delivery;
}
