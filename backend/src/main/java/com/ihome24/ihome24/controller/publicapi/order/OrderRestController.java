package com.ihome24.ihome24.controller.publicapi.order;

import com.ihome24.ihome24.dto.request.order.CreateOrderRequest;
import com.ihome24.ihome24.dto.request.order.UpdateOrderItemsRequest;
import com.ihome24.ihome24.dto.request.order.UpdateOrderStatusRequest;
import com.ihome24.ihome24.dto.response.order.OrderListResponse;
import com.ihome24.ihome24.dto.response.order.OrderResponse;
import com.ihome24.ihome24.dto.response.order.OrderCountResponse;
import com.ihome24.ihome24.dto.response.order.OrderStatsResponse;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apps/ecommerce/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderRestController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{idOrOrderNumber}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String idOrOrderNumber) {
        Long id;
        try {
            id = Long.parseLong(idOrOrderNumber);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid order id: " + idOrOrderNumber);
        }
        OrderResponse order = orderService.getOrderByIdOrOrderNumber(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/count")
    public ResponseEntity<OrderCountResponse> getTotalOrderCount() {
        return ResponseEntity.ok(orderService.getTotalOrderCount());
    }

    @GetMapping("/stats")
    public ResponseEntity<OrderStatsResponse> getOrderStats() {
        return ResponseEntity.ok(orderService.getOrderStats());
    }

    @GetMapping
    public ResponseEntity<OrderListResponse> getOrders(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) Boolean completed) {

        OrderListResponse response = orderService.getOrders(q, page, itemsPerPage, sortBy, orderBy, completed);
        return ResponseEntity.ok(response);
    }

    /**
     * Заказы конкретного клиента (для админки) по email и/или телефону.
     * Точное совпадение.
     */
    @GetMapping("/by-customer")
    public ResponseEntity<OrderListResponse> getOrdersByCustomer(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy) {

        OrderListResponse response = orderService.getOrdersByEmailOrPhone(email, phone, page, itemsPerPage, sortBy, orderBy != null ? orderBy : "desc");
        return ResponseEntity.ok(response);
    }

    /** Список заказов текущего пользователя (по токену). Только свои заказы. */
    @GetMapping("/me")
    public ResponseEntity<?> getMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Требуется авторизация"));
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Пользователь не найден"));
        }
        OrderListResponse response = orderService.getOrdersByEmailOrPhone(
                user.getEmail(), user.getPhone(), page, itemsPerPage, sortBy, orderBy != null ? orderBy : "desc");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/items")
    public ResponseEntity<OrderResponse> updateOrderItems(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderItemsRequest request) {
        OrderResponse order = orderService.updateOrderItems(id, request.getItems());
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse order = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}