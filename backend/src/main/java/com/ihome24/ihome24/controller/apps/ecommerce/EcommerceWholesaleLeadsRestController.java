package com.ihome24.ihome24.controller.apps.ecommerce;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.entity.wholesale.WholesaleLead;
import com.ihome24.ihome24.entity.wholesale.WholesaleLeadStatus;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.service.wholesale.WholesaleLeadAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apps/ecommerce/wholesale-leads")
@RequiredArgsConstructor
public class EcommerceWholesaleLeadsRestController {

    private final WholesaleLeadAdminService wholesaleLeadAdminService;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(wholesaleLeadAdminService.stats());
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "itemsPerPage", defaultValue = "10") Integer itemsPerPage,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "orderBy", required = false) String orderBy) {

        WholesaleLeadStatus statusEnum = parseStatus(status);
        Page<WholesaleLead> leads = wholesaleLeadAdminService.list(q, statusEnum, page, itemsPerPage, sortBy, orderBy);
        List<Map<String, Object>> items = leads.getContent().stream()
                .map(wholesaleLeadAdminService::toMap)
                .toList();

        return ResponseEntity.ok(Map.of(
                "leads", items,
                "total", leads.getTotalElements()
        ));
    }

    @PostMapping("/{id}/take")
    public ResponseEntity<?> take(@PathVariable Long id) {
        try {
            User manager = currentStaffUser();
            WholesaleLead lead = wholesaleLeadAdminService.takeLead(id, manager);
            return ResponseEntity.ok(wholesaleLeadAdminService.toMap(lead));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {
        try {
            User manager = currentStaffUser();
            WholesaleLead lead = wholesaleLeadAdminService.completeLead(id, manager);
            return ResponseEntity.ok(wholesaleLeadAdminService.toMap(lead));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<?> release(@PathVariable Long id) {
        try {
            User manager = currentStaffUser();
            WholesaleLead lead = wholesaleLeadAdminService.releaseLead(id, manager);
            return ResponseEntity.ok(wholesaleLeadAdminService.toMap(lead));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    private User currentStaffUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new IllegalStateException("Требуется авторизация");
        }
        return userRepository.findByUsernameWithRole(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
    }

    private static WholesaleLeadStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return WholesaleLeadStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
