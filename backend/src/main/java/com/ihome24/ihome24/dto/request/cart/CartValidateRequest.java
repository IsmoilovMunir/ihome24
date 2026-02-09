package com.ihome24.ihome24.dto.request.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartValidateRequest {

    private List<CartItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemRequest {
        @NotNull
        private Long productId;
        @NotNull
        @Positive
        private Integer quantity;
    }
}
