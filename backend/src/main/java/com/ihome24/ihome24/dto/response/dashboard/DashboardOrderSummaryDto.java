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
public class DashboardOrderSummaryDto {

    private Long id;
    private Long orderNumber;
    private String customer;
    private BigDecimal spent;
    private LocalDateTime orderDate;
}
