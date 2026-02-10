package com.ihome24.ihome24.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardTransactionDto {

    private Long orderId;
    private Long orderNumber;
    private String customer;
    private BigDecimal amount;
    private LocalDateTime date;
    private boolean isIncome; // true = доход (заказ), false = расход (нет в нашей модели)
}
