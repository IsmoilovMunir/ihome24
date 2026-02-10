package com.ihome24.ihome24.service.dashboard;

import com.ihome24.ihome24.dto.response.dashboard.*;
import com.ihome24.ihome24.entity.order.Order;
import com.ihome24.ihome24.repository.order.OrderItemRepository;
import com.ihome24.ihome24.repository.order.OrderRepository;
import com.ihome24.ihome24.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long sales = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long customers = orderRepository.countDistinctCustomers();
        long products = productRepository.count();
        BigDecimal revenue = orderRepository.sumSpentByDelivered();
        if (revenue == null) revenue = BigDecimal.ZERO;

        return DashboardStatsResponse.builder()
                .sales(sales)
                .customers(customers)
                .products(products)
                .revenue(revenue)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardFullResponse getFullDashboard() {
        DashboardStatsResponse stats = getStats();

        Map<String, Long> ordersByStatusCount = new LinkedHashMap<>();
        long pendingCount = orderRepository.countByStatus(Order.OrderStatus.PENDING);
        long inProcessingCount = orderRepository.countByStatus(Order.OrderStatus.IN_PROCESSING);
        long inDeliveryCount = orderRepository.countByStatus(Order.OrderStatus.DISPATCHED)
                + orderRepository.countByStatus(Order.OrderStatus.OUT_FOR_DELIVERY)
                + orderRepository.countByStatus(Order.OrderStatus.READY_TO_PICKUP);
        ordersByStatusCount.put("PENDING", pendingCount);
        ordersByStatusCount.put("IN_PROCESSING", inProcessingCount);
        ordersByStatusCount.put("IN_DELIVERY", inDeliveryCount);

        Map<String, List<DashboardOrderSummaryDto>> ordersByStatus = new LinkedHashMap<>();
        ordersByStatus.put("PENDING", toOrderSummaryList(orderRepository.findTop10ByStatusOrderByCreatedAtDesc(Order.OrderStatus.PENDING)));
        ordersByStatus.put("IN_PROCESSING", toOrderSummaryList(orderRepository.findTop10ByStatusOrderByCreatedAtDesc(Order.OrderStatus.IN_PROCESSING)));
        ordersByStatus.put("IN_DELIVERY", toOrderSummaryList(orderRepository.findInDeliveryOrderByCreatedAtDesc(PageRequest.of(0, 10))));

        List<DashboardPopularProductDto> popularProducts = orderItemRepository.findPopularProductsNative().stream()
                .map(row -> DashboardPopularProductDto.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .totalQuantity(((Number) row[2]).longValue())
                        .imageUrl((String) row[3])
                        .build())
                .collect(Collectors.toList());

        List<Order> recentOrders = orderRepository.findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        List<DashboardTransactionDto> recentTransactions = recentOrders.stream()
                .map(o -> DashboardTransactionDto.builder()
                        .orderId(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .customer(o.getCustomer())
                        .amount(o.getSpent())
                        .date(o.getCreatedAt())
                        .isIncome(true)
                        .build())
                .collect(Collectors.toList());

        return DashboardFullResponse.builder()
                .stats(stats)
                .ordersByStatusCount(ordersByStatusCount)
                .ordersByStatus(ordersByStatus)
                .popularProducts(popularProducts)
                .recentTransactions(recentTransactions)
                .build();
    }

    private List<DashboardOrderSummaryDto> toOrderSummaryList(List<Order> orders) {
        return orders.stream()
                .map(o -> DashboardOrderSummaryDto.builder()
                        .id(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .customer(o.getCustomer())
                        .spent(o.getSpent())
                        .orderDate(o.getOrderDate())
                        .build())
                .collect(Collectors.toList());
    }
}
