package com.ihome24.ihome24.dto.request.product;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductInfoRequest {

    private String sku;

    private String brand;

    @JsonAlias("quantity_per_package")
    private Integer quantityPerPackage;

    @NotBlank(message = "Название товара обязательно")
    private String title;

    @Valid
    private CategoryRequest category;

    @Valid
    private DescriptionRequest description;

    @Valid
    private List<CharacteristicRequest> characteristics = new ArrayList<>();

    @Valid
    private MediaRequest media;
}
