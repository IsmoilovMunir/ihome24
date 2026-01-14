package com.ihome24.ihome24.dto.response.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ihome24.ihome24.dto.response.category.CategoryResponse;
import com.ihome24.ihome24.dto.response.collection.CollectionResponse;
import com.ihome24.ihome24.dto.response.supplier.SupplierResponse;
import com.ihome24.ihome24.dto.response.tag.TagResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Long id;
    private String name;
    private String description; // Простое описание для обратной совместимости
    private BigDecimal price;
    private BigDecimal oldPrice;
    private BigDecimal discountedPrice;
    private String sku;
    private String brand; // Добавлено для фронтенда
    private String barcode;
    private Integer stockQuantity;
    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean taxEnabled;
    private String status;
    private String imageUrl;
    private CategoryResponse category;
    private SupplierResponse supplier;
    private CollectionResponse collection;
    
    // Расширенные поля для фронтенда
    private DescriptionResponse descriptionObj; // description.short, description.full, description.benefits
    private List<CharacteristicResponse> characteristics;
    private MediaResponse media; // media.mainImage, media.gallery, media.video
    private List<DetailedVariantResponse> variantsDetailed; // Полная информация о вариантах
    private ReturnsResponse returns;
    private SeoResponse seo;
    
    @Builder.Default
    private List<TagResponse> tags = new ArrayList<>();
    @Builder.Default
    private List<VariantResponse> variants = new ArrayList<>(); // Для обратной совместимости
    @Builder.Default
    private List<ProductImageResponse> images = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Вложенные классы для структурированных данных
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DescriptionResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("short")
        private String shortDescription;
        private String full;
        private List<String> benefits;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacteristicResponse {
        private String key;
        private String name;
        private String value;
        private Boolean filterable;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaResponse {
        private String mainImage;
        private List<String> gallery;
        private String video;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedVariantResponse {
        private String variantId;
        private String sku;
        private Map<String, String> attributes;
        private PriceResponse price;
        private StockResponse stock;
        private BarcodesResponse barcodes;
        private LogisticsResponse logistics;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceResponse {
        private BigDecimal base;
        private BigDecimal sale;
        private String currency;
        private Integer vat;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockResponse {
        private Integer quantity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BarcodesResponse {
        private String skuBarcode;
        private String ean13;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogisticsResponse {
        private String weightKg;
        private DimensionsResponse dimensionsCm;
        private DeliveryResponse delivery;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionsResponse {
        private String length;
        private String width;
        private String height;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryResponse {
        private List<String> methods;
        private String deliveryDays;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnsResponse {
        private Boolean allowed;
        private Integer days;
        private String conditions;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeoResponse {
        private String slug;
        private String metaTitle;
        private String metaDescription;
    }
}
