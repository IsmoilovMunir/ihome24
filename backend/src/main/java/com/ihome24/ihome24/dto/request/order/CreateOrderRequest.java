package com.ihome24.ihome24.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "ФИО обязательно")
    private String fullName;

    @NotBlank(message = "Email обязателен")
    @Email
    private String email;

    @NotBlank(message = "Телефон обязателен")
    private String phone;

    private String address;

    @NotBlank(message = "Способ доставки обязателен")
    private String deliveryMethod; // "delivery" | "pickup"

    private String pickupAddress;

    @NotBlank(message = "Способ оплаты обязателен")
    private String paymentMethod; // "cash" etc.

    private String comment;

    @NotEmpty(message = "Корзина не может быть пустой")
    @Valid
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
