package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class VariantRequest {

    private String variantId;

    private String sku;

    private Map<String, String> attributes = new HashMap<>();

    @NotNull(message = "Цена обязательна")
    @Valid
    private PriceRequest price;

    @Valid
    private StockRequest stock;

    @Valid
    private BarcodesRequest barcodes;

    @Valid
    private LogisticsRequest logistics;
}
