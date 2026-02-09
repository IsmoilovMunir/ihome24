package com.ihome24.ihome24.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Статус не может быть пустым")
    private String status; // PENDING, IN_PROCESSING, DISPATCHED, OUT_FOR_DELIVERY, READY_TO_PICKUP, DELIVERED
}
