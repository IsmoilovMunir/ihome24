package com.ihome24.ihome24.controller.publicapi.auth;

import com.ihome24.ihome24.entity.user.Role;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.RoleRepository;
import com.ihome24.ihome24.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для первоначальной настройки системы
 * Создает первого администратора, если его еще нет
 */
@RestController
@RequestMapping("/api/publicapi/bootstrap")
@RequiredArgsConstructor
@Slf4j
public class BootstrapController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    public ResponseEntity<?> createFirstAdmin() {
        // Проверяем, есть ли уже администраторы
        if (userRepository.count() > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Users already exist. Use /api/publicapi/auth/register-admin to create new admin.");
            response.put("totalUsers", userRepository.count());
            return ResponseEntity.ok(response);
        }

        // Ищем роль admin
        Role adminRole = roleRepository.findByName("admin")
                .orElse(null);

        if (adminRole == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Admin role not found. Please ensure roles are initialized.");
            return ResponseEntity.status(500).body(error);
        }

        // Создаем первого администратора
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .email("ismoilov.munir97@gmail.com")
                .fullName("Администратор")
                .role(adminRole)
                .status(User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .currentPlan("enterprise")
                .billing("Автоматическое списание")
                .build();

        admin = userRepository.save(admin);

        log.info("✓ First admin created: username=admin, email=ismoilov.munir97@gmail.com, password=admin");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "First admin created successfully");
        response.put("username", admin.getUsername());
        response.put("email", admin.getEmail());
        response.put("password", "admin");
        response.put("note", "Please change the password after first login");

        return ResponseEntity.ok(response);
    }
}
