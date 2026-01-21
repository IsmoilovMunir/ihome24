package com.ihome24.ihome24.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductRequest {

    // Информация о компании теперь берется из настроек компании (company-settings)
    // Это поле оставлено для обратной совместимости, но игнорируется при создании товара
    @Valid
    private CompanyRequest company;

    @NotNull(message = "Информация о товаре обязательна")
    @Valid
    private ProductInfoRequest product;

    @Valid
    private List<VariantRequest> variants = new ArrayList<>();

    @Valid
    private ReturnsRequest returns;

    @Valid
    private SeoRequest seo;

    private String status = "draft";
}
