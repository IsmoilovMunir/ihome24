package com.ihome24.ihome24.dto.response.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Ответ PATCH /api/admin/products/{id}/seo (спецификация §5.2).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSeoPatchResponse {

    private boolean success;
    private String slug;
    private boolean redirectCreated;

    /** Предупреждения (не блокируют сохранение), для админки */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /** Полный товар (опционально, для админ-формы) */
    private ProductResponse product;
}
