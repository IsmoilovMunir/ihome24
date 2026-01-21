package com.ihome24.ihome24.service.order;

import com.ihome24.ihome24.dto.response.order.OrderListResponse;
import com.ihome24.ihome24.dto.response.order.OrderResponse;
import com.ihome24.ihome24.entity.order.Order;
import com.ihome24.ihome24.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderListResponse getOrders(String searchQuery, Integer page, Integer itemsPerPage, String sortBy, String orderBy) {
        // Create pageable with sorting
        Sort.Direction direction = "desc".equalsIgnoreCase(orderBy) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = getSortField(sortBy);
        Pageable pageable = PageRequest.of(page - 1, itemsPerPage, Sort.by(direction, sortField));

        // Get orders with search
        Page<Order> orderPage = orderRepository.findOrdersWithSearch(searchQuery, pageable);

        // Map to response
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return OrderListResponse.builder()
                .orders(orderResponses)
                .total(orderPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    private String getSortField(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return "createdAt";
        }

        switch (sortBy) {
            case "order":
                return "orderNumber";
            case "date":
                return "orderDate";
            case "customers":
                return "customer";
            case "status":
                return "status";
            default:
                return "createdAt";
        }
    }

    private OrderResponse mapToResponse(Order order) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        return OrderResponse.builder()
                .id(order.getId())
                .order(order.getOrderNumber())
                .customer(order.getCustomer())
                .email(order.getEmail())
                .avatar(order.getAvatarUrl())
                .payment(mapPaymentStatusToInt(order.getPayment()))
                .status(mapOrderStatusToString(order.getStatus()))
                .spent(order.getSpent())
                .method(mapPaymentMethodToString(order.getMethod()))
                .date(order.getOrderDate().format(dateFormatter))
                .methodNumber(order.getMethodNumber())
                .build();
    }

    private Integer mapPaymentStatusToInt(Order.PaymentStatus status) {
        switch (status) {
            case PAID: return 1;
            case PENDING: return 2;
            case CANCELLED: return 3;
            case FAILED: return 4;
            default: return 2;
        }
    }

    private String mapOrderStatusToString(Order.OrderStatus status) {
        switch (status) {
            case DELIVERED: return "Delivered";
            case OUT_FOR_DELIVERY: return "Out for Delivery";
            case READY_TO_PICKUP: return "Ready to Pickup";
            case DISPATCHED: return "Dispatched";
            default: return "Delivered";
        }
    }

    private String mapPaymentMethodToString(Order.PaymentMethod method) {
        switch (method) {
            case PAYPAL: return "paypalLogo";
            case MASTERCARD: return "mastercard";
            default: return "paypalLogo";
        }
    }
}