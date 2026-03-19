package com.ihome24.ihome24.controller.apps.ecommerce;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.order.OrderRepository;
import com.ihome24.ihome24.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API для страницы клиентов в админ-панели.
 * Сейчас возвращает базовые данные из таблицы users.
 */
@RestController
@RequestMapping("/api/apps/ecommerce/customers")
@RequiredArgsConstructor
public class EcommerceCustomersRestController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private static final String CUSTOMER_ROLE = "users";

    @GetMapping
    public ResponseEntity<?> listCustomers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "itemsPerPage", required = false, defaultValue = "10") Integer itemsPerPage,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "orderBy", required = false) String orderBy
    ) {
        int size = itemsPerPage != null && itemsPerPage > 0 ? itemsPerPage : 10;
        int pageIndex = page != null && page > 0 ? page - 1 : 0;

        Pageable pageable = PageRequest.of(pageIndex, size, resolveSort(sortBy, orderBy));

        // Берём только клиентов магазина (роль users), админов сюда не показываем.
        Page<User> users = userRepository.findUsersWithFilters(q, CUSTOMER_ROLE, null, null, pageable);

        List<Map<String, Object>> customers = users.getContent().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("customer", u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername());
            m.put("customerId", u.getId());
            m.put("email", u.getEmail());
            m.put("avatar", u.getAvatar());
            m.put("country", u.getCountry() != null ? u.getCountry() : "—");
            m.put("countryFlag", ""); // можно добавить позже
            String email = u.getEmail();
            String phone = u.getPhone();
            m.put("order", orderRepository.countByEmailOrPhone(email, phone));
            m.put("totalSpent", orderRepository.sumSpentByEmailOrPhone(email, phone));
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "customers", customers,
                "total", users.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable Long id) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) {
            return ResponseEntity.notFound().build();
        }
        if (u.getRole() == null || u.getRole().getName() == null || !CUSTOMER_ROLE.equals(u.getRole().getName())) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> customer = new HashMap<>();
        customer.put("customer", u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername());
        customer.put("customerId", u.getId());
        customer.put("email", u.getEmail());
        customer.put("phone", u.getPhone());
        customer.put("avatar", u.getAvatar());
        customer.put("country", u.getCountry() != null ? u.getCountry() : "—");
        customer.put("countryFlag", "");
        customer.put("status", u.getStatus() != null ? u.getStatus().name() : "ACTIVE");
        customer.put("contact", u.getContact());
        customer.put("order", orderRepository.countByEmailOrPhone(u.getEmail(), u.getPhone()));
        customer.put("totalSpent", orderRepository.sumSpentByEmailOrPhone(u.getEmail(), u.getPhone()));

        return ResponseEntity.ok(customer);
    }

    private Sort resolveSort(String sortBy, String orderBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(orderBy) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Привязываем ключи из админки к полям User
        String field = switch (sortBy) {
            case "customer", "customerId" -> "id";
            case "country" -> "country";
            default -> "id";
        };

        return Sort.by(dir, field);
    }
}

