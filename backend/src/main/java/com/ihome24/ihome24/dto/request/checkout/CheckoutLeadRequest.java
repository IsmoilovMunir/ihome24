package com.ihome24.ihome24.dto.request.checkout;

import com.ihome24.ihome24.dto.request.order.CreateOrderRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutLeadRequest {
    @NotBlank(message = "ФИО обязательно")
    private String fullName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Введите корректный email")
    private String email;

    @NotBlank(message = "Номер телефона обязателен")
    private String phone;

    /** contacts | delivery | payment — для аналитики в админке */
    private String step;

    /** Позиции корзины для предварительного заказа */
    @Valid
    private List<CreateOrderRequest.OrderItemRequest> items;
}
