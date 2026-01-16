package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

@Data
public class ReturnsRequest {
    private Boolean allowed = true;
    private Integer days = 14;
    private String conditions;
}
