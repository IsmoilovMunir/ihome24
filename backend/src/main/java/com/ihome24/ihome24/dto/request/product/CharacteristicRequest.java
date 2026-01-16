package com.ihome24.ihome24.dto.request.product;

import lombok.Data;

@Data
public class CharacteristicRequest {
    private String key;
    private String name;
    private String value;
    private Boolean filterable = true;
}
