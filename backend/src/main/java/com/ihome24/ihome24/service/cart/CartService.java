package com.ihome24.ihome24.service.cart;

import com.ihome24.ihome24.dto.request.cart.CartValidateRequest;
import com.ihome24.ihome24.dto.response.cart.CartValidateResponse;
import com.ihome24.ihome24.dto.response.product.ProductResponse;
import com.ihome24.ihome24.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис валидации корзины.
 * Проверяет, что товары существуют, активны и возвращает актуальные данные (цена, остаток).
 * Товары, удалённые из админки или деактивированные, исключаются из результата.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductService productService;

    @Transactional(readOnly = true)
    public CartValidateResponse validateCart(CartValidateRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return CartValidateResponse.builder()
                    .validItems(new ArrayList<>())
                    .removedIds(new ArrayList<>())
                    .build();
        }

        List<Long> productIds = request.getItems().stream()
                .map(CartValidateRequest.CartItemRequest::getProductId)
                .distinct()
                .toList();

        List<ProductResponse> validProducts = productService.getActiveProductsByIds(productIds);
        Set<Long> validIds = validProducts.stream()
                .map(ProductResponse::getId)
                .collect(Collectors.toSet());

        Map<Long, ProductResponse> productMap = validProducts.stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));

        Map<Long, Integer> quantityByProduct = new HashMap<>();
        List<Long> removedIds = new ArrayList<>();

        for (CartValidateRequest.CartItemRequest item : request.getItems()) {
            Long productId = item.getProductId();
            Integer qty = item.getQuantity() != null ? item.getQuantity() : 0;
            if (validIds.contains(productId) && qty > 0) {
                quantityByProduct.merge(productId, qty, (a, b) -> a + b);
            } else {
                removedIds.add(productId);
            }
        }

        List<CartValidateResponse.CartItemResponse> validItems = quantityByProduct.entrySet().stream()
                .map(e -> {
                    ProductResponse product = productMap.get(e.getKey());
                    int requestedQty = e.getValue();
                    int maxQty = product.getStockQuantity() != null ? product.getStockQuantity() : Integer.MAX_VALUE;
                    int finalQty = Math.min(requestedQty, maxQty);
                    return new CartValidateResponse.CartItemResponse(product, finalQty);
                })
                .toList();

        return CartValidateResponse.builder()
                .validItems(validItems)
                .removedIds(removedIds)
                .build();
    }
}
