package com.ihome24.ihome24.controller.publicapi.order;

import com.ihome24.ihome24.dto.response.order.OrderListResponse;
import com.ihome24.ihome24.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apps/ecommerce/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderRestController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<OrderListResponse> getOrders(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy) {

        OrderListResponse response = orderService.getOrders(q, page, itemsPerPage, sortBy, orderBy);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}