package com.ihome24.ihome24.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * N-2: lookup редиректа slug → newSlug (null = канонический slug, редирект не нужен).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSlugRedirectLookupResponse {
  private String newSlug;
}
