package com.ihome24.ihome24.service.checkout;

import com.ihome24.ihome24.dto.request.order.CreateOrderRequest;
import com.ihome24.ihome24.entity.order.Order;
import com.ihome24.ihome24.entity.order.OrderItem;
import com.ihome24.ihome24.entity.product.Product;
import com.ihome24.ihome24.repository.order.OrderRepository;
import com.ihome24.ihome24.repository.product.ProductRepository;
import com.ihome24.ihome24.service.auth.PhoneAuthService;
import com.ihome24.ihome24.service.company.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutPreliminaryOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CompanySettingsService companySettingsService;
    private final PhoneAuthService phoneAuthService;

    public record PreliminaryResult(Long orderId, Long orderNumber) {}

    @Transactional
    public PreliminaryResult upsertPreliminaryOrder(
            String fullName,
            String email,
            String phone,
            List<CreateOrderRequest.OrderItemRequest> items) {

        String normalizedPhone = phoneAuthService.normalizePhone(phone);
        String emailTrim = email != null ? email.trim() : "";

        Order order = orderRepository
                .findFirstByStatusAndPhoneOrderByUpdatedAtDesc(Order.OrderStatus.PRELIMINARY, normalizedPhone)
                .or(() -> orderRepository.findFirstByStatusAndEmailIgnoreCaseOrderByUpdatedAtDesc(
                        Order.OrderStatus.PRELIMINARY, emailTrim))
                .orElse(null);

        if (order == null) {
            Long nextOrderNumber = orderRepository.findMaxOrderNumber();
            nextOrderNumber = (nextOrderNumber != null ? nextOrderNumber : 0L) + 1L;

            order = Order.builder()
                    .orderNumber(nextOrderNumber)
                    .customer(fullName != null ? fullName.trim() : "")
                    .email(emailTrim)
                    .phone(normalizedPhone)
                    .payment(Order.PaymentStatus.PENDING)
                    .status(Order.OrderStatus.PRELIMINARY)
                    .method(Order.PaymentMethod.MASTERCARD)
                    .spent(BigDecimal.ZERO)
                    .orderDate(LocalDateTime.now())
                    .build();
            order = orderRepository.save(order);
        } else {
            order.setCustomer(fullName != null ? fullName.trim() : order.getCustomer());
            order.setEmail(emailTrim);
            order.setPhone(normalizedPhone);
        }

        if (items != null && !items.isEmpty()) {
            replaceOrderItems(order, items);
        }

        order.setSpent(calculateOrderTotal(order));
        order = orderRepository.save(order);

        return new PreliminaryResult(order.getId(), order.getOrderNumber());
    }

    private void replaceOrderItems(Order order, List<CreateOrderRequest.OrderItemRequest> items) {
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
        } else {
            order.getItems().clear();
        }

        for (CreateOrderRequest.OrderItemRequest itemReq : items) {
            if (itemReq.getProductId() == null || itemReq.getQuantity() == null || itemReq.getQuantity() < 1) {
                continue;
            }
            Product product = productRepository.findById(itemReq.getProductId()).orElse(null);
            if (product == null || product.getIsActive() == null || !product.getIsActive()) {
                continue;
            }
            BigDecimal basePrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            BigDecimal price = companySettingsService.getUnitPriceForQuantity(basePrice, itemReq.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .quantity(itemReq.getQuantity())
                    .price(price)
                    .build();
            order.getItems().add(orderItem);
        }
    }

    private BigDecimal calculateOrderTotal(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getPrice() == null || item.getQuantity() == null) continue;
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }
}
