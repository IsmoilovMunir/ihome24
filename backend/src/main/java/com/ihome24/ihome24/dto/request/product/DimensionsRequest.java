package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DimensionsRequest {
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
}
