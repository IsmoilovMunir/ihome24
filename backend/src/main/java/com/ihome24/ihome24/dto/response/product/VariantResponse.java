package com.ihome24.ihome24.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantResponse {
    private String variantId;
    private String sku;
    private Map<String, String> attributes = new HashMap<>();
    private PriceResponse price;
    private StockResponse stock;
    private BarcodesResponse barcodes;
    private LogisticsResponse logistics;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceResponse {
        private BigDecimal base;
        private BigDecimal sale;
        private String currency;
        private Integer vat;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockResponse {
        private Integer quantity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BarcodesResponse {
        private String skuBarcode;
        private String ean13;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LogisticsResponse {
        private BigDecimal weightKg;
        private DimensionsResponse dimensionsCm;
        private DeliveryResponse delivery;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DimensionsResponse {
            private BigDecimal length;
            private BigDecimal width;
            private BigDecimal height;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DeliveryResponse {
            private List<String> methods;
            private String deliveryDays;
        }
    }
}
