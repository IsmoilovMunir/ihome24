package com.ihome24.ihome24.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacteristicResponse {
    private String key;
    private String name;
    private String value;
    private Boolean filterable;
}
