package com.ihome24.ihome24.controller.publicapi.invoice;

import com.ihome24.ihome24.dto.response.invoice.InvoiceListResponse;
import com.ihome24.ihome24.dto.response.invoice.InvoicePreviewResponse;
import com.ihome24.ihome24.dto.response.invoice.InvoiceResponse;
import com.ihome24.ihome24.dto.response.order.OrderResponse;
import com.ihome24.ihome24.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Эндпоинт для совместимости с админкой (EcommerceInvoiceTable).
 * Возвращает заказы в формате счётов (invoices).
 */
@RestController
@RequestMapping("/api/apps/invoice")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceRestController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<InvoiceListResponse> getInvoices(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer itemsPerPage,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String orderBy) {

        var orderList = orderService.getOrders(q, page, itemsPerPage, sortBy, orderBy, null);
        List<InvoiceResponse> invoices = orderList.getOrders().stream()
                .map(this::mapOrderToInvoice)
                .collect(Collectors.toList());

        return ResponseEntity.ok(InvoiceListResponse.builder()
                .invoices(invoices)
                .totalInvoices(orderList.getTotal() != null ? orderList.getTotal() : 0L)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoicePreviewResponse> getInvoiceById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderByIdOrOrderNumber(id);

        LocalDateTime orderDate = parseOrderDate(order.getDate());
        LocalDateTime dueDate = orderDate != null ? orderDate.plusDays(30) : LocalDateTime.now().plusDays(30);

        String totalDue = order.getSpent() != null
                ? String.format("%.2f ₽", order.getSpent())
                : "0 ₽";

        InvoicePreviewResponse response = InvoicePreviewResponse.builder()
                .invoice(InvoicePreviewResponse.InvoicePreview.builder()
                        .id(order.getId())
                        .issuedDate(orderDate != null ? orderDate : LocalDateTime.now())
                        .dueDate(dueDate)
                        .client(InvoicePreviewResponse.ClientInfo.builder()
                                .name(order.getCustomer() != null ? order.getCustomer() : "")
                                .company("")
                                .address(order.getAddress() != null ? order.getAddress() : "")
                                .country("Россия")
                                .contact(order.getPhone() != null ? order.getPhone() : "")
                                .companyEmail(order.getEmail() != null ? order.getEmail() : "")
                                .build())
                        .build())
                .paymentDetails(InvoicePreviewResponse.PaymentDetails.builder()
                        .totalDue(totalDue)
                        .bankName("Сбербанк России")
                        .country("Россия")
                        .iban("ETD95476213874685")
                        .swiftCode("SABRRUMM")
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

    private LocalDateTime parseOrderDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return java.time.LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("M/d/yyyy"))
                    .atStartOfDay();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private InvoiceResponse mapOrderToInvoice(OrderResponse order) {
        BigDecimal spent = order.getSpent() != null ? order.getSpent() : BigDecimal.ZERO;
        // balance = spent если не оплачено, 0 если оплачено (payment=1)
        BigDecimal balance = (order.getPayment() != null && order.getPayment() == 1)
                ? BigDecimal.ZERO
                : spent;

        return InvoiceResponse.builder()
                .id(order.getId())
                .client(new InvoiceResponse.ClientInfo(
                        order.getCustomer() != null ? order.getCustomer() : "",
                        order.getEmail() != null ? order.getEmail() : ""))
                .total(spent)
                .issuedDate(order.getDate())
                .balance(balance)
                .invoiceStatus(order.getStatus() != null ? order.getStatus() : "Pending")
                .build();
    }
}
