package com.ihome24.ihome24.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeoResponse {
    /** Канонический slug: kukhonnyi-nozh-chef-20sm */
    private String slug;
    /** То же, что slug (для совместимости с фронтом) */
    private String pathSegment;
    private String metaTitle;
    private String metaDescription;
    /** Open Graph image (из og_image или главного фото товара) */
    private String ogImage;

    /** Значения из БД (null = пусто, для индикатора SEO в админ-списке). */
    private String storedSlug;
    private String storedMetaTitle;
    private String storedMetaDescription;
}
