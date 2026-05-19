package com.ihome24.ihome24.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSeoGenerateAllResult {
    private int productsUpdated;
    private int slugsGenerated;
    private int metaTitlesGenerated;
    private int metaDescriptionsGenerated;
}
