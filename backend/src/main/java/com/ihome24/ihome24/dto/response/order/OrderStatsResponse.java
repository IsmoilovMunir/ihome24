package com.ihome24.ihome24.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatsResponse {

    private long awaitingPayment;  // Ожидает оплаты (PENDING)
    private long completed;        // Завершено (DELIVERED)
    private long returned;         // Возвращено (CANCELLED)
    private long failed;           // Неудачно (FAILED)
}
