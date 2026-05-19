package com.ihome24.ihome24.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSlugResolveResponse {
    private ProductResponse product;
    /** true — нужен 301 на canonicalPath */
    private boolean redirect;
    /** Канонический путь витрины, напр. /product/kukhonnyi-nozh-chef-20sm */
    private String canonicalPath;

    /** Актуальный slug для HTTP 301 Location: /api/products/{redirectTargetSlug} */
    private String redirectTargetSlug;
}
