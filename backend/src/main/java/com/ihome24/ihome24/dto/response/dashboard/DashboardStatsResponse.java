package com.ihome24.ihome24.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long sales;       // Продажи (кол-во доставленных заказов)
    private long customers;   // Клиенты (уникальные по email)
    private long products;    // Товары
    private BigDecimal revenue; // Доход (сумма доставленных заказов)
}
