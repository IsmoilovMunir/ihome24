package com.ihome24.ihome24.service.wholesale;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.entity.wholesale.WholesaleLead;
import com.ihome24.ihome24.entity.wholesale.WholesaleLeadStatus;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.repository.wholesale.WholesaleLeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WholesaleLeadAdminService {

    private final WholesaleLeadRepository wholesaleLeadRepository;
    private final UserRepository userRepository;

    public Page<WholesaleLead> list(String q, WholesaleLeadStatus status, int page, int size, String sortBy, String orderBy) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1), resolveSort(sortBy, orderBy));
        String query = q != null ? q.trim() : null;
        if (query != null && query.isEmpty()) {
            query = null;
        }
        return wholesaleLeadRepository.findWithFilters(query, status, pageable);
    }

    public Map<String, Long> stats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("new", wholesaleLeadRepository.countByStatus(WholesaleLeadStatus.NEW));
        stats.put("inProgress", wholesaleLeadRepository.countByStatus(WholesaleLeadStatus.IN_PROGRESS));
        stats.put("done", wholesaleLeadRepository.countByStatus(WholesaleLeadStatus.DONE));
        stats.put("total", wholesaleLeadRepository.count());
        return stats;
    }

    @Transactional
    public WholesaleLead takeLead(Long leadId, User manager) {
        requireStaff(manager);
        WholesaleLead lead = wholesaleLeadRepository.findByIdWithManager(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

        if (lead.getStatus() == WholesaleLeadStatus.DONE) {
            throw new IllegalStateException("Заявка уже завершена");
        }

        if (lead.getAssignedManager() != null
                && !lead.getAssignedManager().getId().equals(manager.getId())
                && !isAdmin(manager)) {
            throw new IllegalStateException("Заявка уже в работе у другого менеджера");
        }

        lead.setAssignedManager(manager);
        lead.setAssignedAt(LocalDateTime.now());
        lead.setStatus(WholesaleLeadStatus.IN_PROGRESS);
        return wholesaleLeadRepository.save(lead);
    }

    @Transactional
    public WholesaleLead completeLead(Long leadId, User manager) {
        requireStaff(manager);
        WholesaleLead lead = wholesaleLeadRepository.findByIdWithManager(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

        if (lead.getAssignedManager() != null
                && !lead.getAssignedManager().getId().equals(manager.getId())
                && !isAdmin(manager)) {
            throw new IllegalStateException("Заявку может завершить только назначенный менеджер");
        }

        lead.setStatus(WholesaleLeadStatus.DONE);
        return wholesaleLeadRepository.save(lead);
    }

    @Transactional
    public WholesaleLead releaseLead(Long leadId, User manager) {
        requireStaff(manager);
        WholesaleLead lead = wholesaleLeadRepository.findByIdWithManager(leadId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

        if (lead.getAssignedManager() != null
                && !lead.getAssignedManager().getId().equals(manager.getId())
                && !isAdmin(manager)) {
            throw new IllegalStateException("Снять заявку может только назначенный менеджер или администратор");
        }

        lead.setAssignedManager(null);
        lead.setAssignedAt(null);
        lead.setStatus(WholesaleLeadStatus.NEW);
        return wholesaleLeadRepository.save(lead);
    }

    public Map<String, Object> toMap(WholesaleLead lead) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", lead.getId());
        m.put("name", lead.getName());
        m.put("phone", lead.getPhone());
        m.put("inn", lead.getInn());
        m.put("message", lead.getMessage());
        m.put("status", lead.getStatus() != null ? lead.getStatus().name() : WholesaleLeadStatus.NEW.name());
        m.put("statusLabel", statusLabel(lead.getStatus()));
        m.put("createdAt", lead.getCreatedAt());
        m.put("assignedAt", lead.getAssignedAt());
        User manager = lead.getAssignedManager();
        if (manager != null) {
            m.put("managerId", manager.getId());
            m.put("managerName", displayName(manager));
        } else {
            m.put("managerId", null);
            m.put("managerName", null);
        }
        return m;
    }

    public static String statusLabel(WholesaleLeadStatus status) {
        if (status == null) {
            return "Новая";
        }
        return switch (status) {
            case NEW -> "Новая";
            case IN_PROGRESS -> "В работе";
            case DONE -> "Завершена";
        };
    }

    public static String displayName(User user) {
        if (user == null) {
            return null;
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getUsername();
    }

    private static void requireStaff(User user) {
        if (user == null || user.getRole() == null) {
            throw new IllegalStateException("Недостаточно прав");
        }
        String role = user.getRole().getName();
        if (!"admin".equals(role) && !"manager".equals(role)) {
            throw new IllegalStateException("Недостаточно прав");
        }
    }

    private static boolean isAdmin(User user) {
        return user.getRole() != null && "admin".equals(user.getRole().getName());
    }

    private static Sort resolveSort(String sortBy, String orderBy) {
        String property = switch (sortBy != null ? sortBy : "") {
            case "name" -> "name";
            case "phone" -> "phone";
            case "status" -> "status";
            case "assignedAt" -> "assignedAt";
            default -> "createdAt";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(orderBy) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
