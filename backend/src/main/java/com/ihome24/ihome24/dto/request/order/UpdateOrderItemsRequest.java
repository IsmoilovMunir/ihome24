package com.ihome24.ihome24.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemsRequest {

    @Valid
    private List<OrderItemUpdate> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemUpdate {
        @NotNull(message = "productId обязателен")
        private Long productId;

        @NotNull(message = "Количество обязательно")
        @Min(value = 1, message = "Количество должно быть не менее 1")
        private Integer quantity;
    }
}
