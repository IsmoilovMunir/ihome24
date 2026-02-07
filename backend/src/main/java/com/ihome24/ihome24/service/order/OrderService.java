package com.ihome24.ihome24.service.order;

import com.ihome24.ihome24.dto.request.order.CreateOrderRequest;
import com.ihome24.ihome24.dto.response.order.OrderItemResponse;
import com.ihome24.ihome24.dto.response.order.OrderListResponse;
import com.ihome24.ihome24.dto.response.order.OrderResponse;
import com.ihome24.ihome24.entity.order.Order;
import com.ihome24.ihome24.entity.order.OrderItem;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.exception.ResourceNotFoundException;
import com.ihome24.ihome24.repository.order.OrderRepository;
import com.ihome24.ihome24.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

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

    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdOrOrderNumber(Long idOrOrderNumber) {
        Order order = orderRepository.findByIdWithItems(idOrOrderNumber)
                .or(() -> orderRepository.findByOrderNumberWithItems(idOrOrderNumber))
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден: " + idOrOrderNumber));
        return mapToDetailsResponse(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Заказ не найден: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Long maxNum = orderRepository.findMaxOrderNumber();
        Long nextOrderNumber = (maxNum != null ? maxNum + 1 : 1L);

        String address = "delivery".equals(request.getDeliveryMethod())
                ? request.getAddress()
                : request.getPickupAddress();

        Order.PaymentMethod paymentMethod = "cash".equalsIgnoreCase(request.getPaymentMethod())
                ? Order.PaymentMethod.CASH
                : Order.PaymentMethod.MASTERCARD;

        BigDecimal totalSpent = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + itemReq.getProductId()));
            if (product.getIsActive() == null || !product.getIsActive()) {
                throw new IllegalArgumentException("Товар недоступен для заказа: " + product.getName());
            }
            BigDecimal price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalSpent = totalSpent.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .quantity(itemReq.getQuantity())
                    .price(price)
                    .build();
            items.add(orderItem);
        }

        Order order = Order.builder()
                .orderNumber(nextOrderNumber)
                .customer(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(address)
                .deliveryMethod(request.getDeliveryMethod())
                .comment(request.getComment())
                .payment(Order.PaymentStatus.PENDING)
                .status(Order.OrderStatus.PENDING)
                .spent(totalSpent)
                .method(paymentMethod)
                .orderDate(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrder(order);
            order.getItems().add(item);
        }
        order = orderRepository.save(order);

        return mapToResponse(order);
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

    private OrderResponse mapToDetailsResponse(Order order) {
        OrderResponse response = mapToResponse(order);
        response.setPhone(order.getPhone());
        response.setAddress(order.getAddress());

        List<OrderItemResponse> itemResponses = order.getItems() != null
                ? order.getItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .total(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .build())
                    .collect(Collectors.toList())
                : List.of();
        response.setItems(itemResponses);

        return response;
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
        if (status == null) return "Delivered";
        switch (status) {
            case PENDING: return "Pending";
            case DELIVERED: return "Delivered";
            case OUT_FOR_DELIVERY: return "Out for Delivery";
            case READY_TO_PICKUP: return "Ready to Pickup";
            case DISPATCHED: return "Dispatched";
            default: return "Delivered";
        }
    }

    private String mapPaymentMethodToString(Order.PaymentMethod method) {
        if (method == null) return "paypalLogo";
        switch (method) {
            case PAYPAL: return "paypalLogo";
            case MASTERCARD: return "mastercard";
            case CASH: return "cash";
            default: return "paypalLogo";
        }
    }
}