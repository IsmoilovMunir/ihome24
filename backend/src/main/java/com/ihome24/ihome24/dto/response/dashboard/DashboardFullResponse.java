package com.ihome24.ihome24.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardFullResponse {

    private DashboardStatsResponse stats;
    private Map<String, Long> ordersByStatusCount;  // PENDING, IN_PROCESSING, IN_DELIVERY
    private Map<String, List<DashboardOrderSummaryDto>> ordersByStatus;  // списки заказов по вкладкам
    private List<DashboardPopularProductDto> popularProducts;
    private List<DashboardTransactionDto> recentTransactions;
}
