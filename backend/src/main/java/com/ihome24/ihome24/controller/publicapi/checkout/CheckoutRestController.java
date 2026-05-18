package com.ihome24.ihome24.controller.publicapi.checkout;

import com.ihome24.ihome24.config.security.JwtTokenService;
import com.ihome24.ihome24.dto.request.checkout.CheckoutLeadRequest;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.service.checkout.CheckoutGuestService;
import com.ihome24.ihome24.service.checkout.CheckoutPreliminaryOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сохранение контактов гостя при оформлении заказа (лид в разделе «Клиенты» админки).
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutRestController {

    private final CheckoutGuestService checkoutGuestService;
    private final CheckoutPreliminaryOrderService checkoutPreliminaryOrderService;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/lead")
    public ResponseEntity<?> saveLead(
            @Valid @RequestBody CheckoutLeadRequest request,
            @RequestParam(defaultValue = "false") boolean issueToken) {
        try {
            CheckoutGuestService.UpsertResult result = checkoutGuestService.upsertGuestCustomer(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPhone()
            );
            User user = userRepository.findByUsernameWithRoleAndPermissions(result.user().getUsername())
                    .orElse(result.user());

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("userId", user.getId());
            body.put("created", result.created());
            body.put("step", request.getStep() != null ? request.getStep() : "contacts");

            if (request.getItems() != null && !request.getItems().isEmpty()) {
                CheckoutPreliminaryOrderService.PreliminaryResult preliminary =
                        checkoutPreliminaryOrderService.upsertPreliminaryOrder(
                                request.getFullName(),
                                request.getEmail(),
                                request.getPhone(),
                                request.getItems());
                body.put("preliminaryOrderId", preliminary.orderId());
                body.put("orderNumber", preliminary.orderNumber());
            }

            log.info("Checkout lead saved: userId={}, created={}, step={}",
                    user.getId(), result.created(), request.getStep());

            if (issueToken) {
                return ResponseEntity.ok(buildSessionResponse(user, body));
            }
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            String field = e.getMessage().contains("телефон") ? "phone"
                    : e.getMessage().contains("почт") || e.getMessage().contains("email") ? "email"
                    : "fullName";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", Map.of(field, new String[]{e.getMessage()})));
        }
    }

    private Map<String, Object> buildSessionResponse(User user, Map<String, Object> base) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("fullName", user.getFullName());
        userData.put("avatar", user.getAvatar());
        userData.put("role", user.getRole() != null ? user.getRole().getName() : null);
        userData.put("status", user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
        userData.put("passwordChangeRequired", Boolean.TRUE.equals(user.getPasswordChangeRequired()));

        String accessToken = jwtTokenService.generateAccessToken(user);
        base.put("accessToken", accessToken);
        base.put("userData", userData);
        base.put("userAbilityRules", List.of());
        return base;
    }
}
