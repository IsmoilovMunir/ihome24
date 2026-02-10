package com.ihome24.ihome24.dto.response.cart;

import com.ihome24.ihome24.dto.response.product.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartValidateResponse {

    /** Валидные товары с актуальными данными (цена, остаток и т.д.) */
    private List<CartItemResponse> validItems;

    /** ID товаров, удалённых из админки или неактивных — их нужно убрать из корзины */
    private List<Long> removedIds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
        private ProductResponse product;
        private Integer quantity;
    }
}
