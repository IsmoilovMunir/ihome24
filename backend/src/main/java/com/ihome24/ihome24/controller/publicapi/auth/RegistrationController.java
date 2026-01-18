package com.ihome24.ihome24.controller.publicapi.auth;

import com.ihome24.ihome24.dto.request.auth.AdminRegistrationRequest;
import com.ihome24.ihome24.dto.request.user.UserRequest;
import com.ihome24.ihome24.dto.response.user.UserResponse;
import com.ihome24.ihome24.entity.user.Role;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.RoleRepository;
import com.ihome24.ihome24.repository.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/publicapi/auth")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest request) {
        // Проверка на существование username
        if (userRepository.existsByUsername(request.getUsername())) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("username", new String[]{"Username already exists"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", errors));
        }
        // Проверка на существование email
        if (userRepository.existsByEmail(request.getEmail())) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("email", new String[]{"Email already exists"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", errors));
        }

        // Определяем роль для нового пользователя
        Role role;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
        } else {
            // По умолчанию создаем пользователя с ролью "users"
            role = roleRepository.findByName("users")
                    .orElseThrow(() -> new RuntimeException("Default 'users' role not found"));
        }

        // Определяем статус пользователя
        User.UserStatus userStatus = User.UserStatus.ACTIVE; // По умолчанию активен
        if (request.getStatus() != null) {
            try {
                userStatus = User.UserStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                userStatus = User.UserStatus.ACTIVE;
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword() != null && !request.getPassword().isEmpty() 
                        ? request.getPassword() : "password"))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .avatar(request.getAvatar())
                .company(request.getCompany())
                .country(request.getCountry())
                .contact(request.getContact())
                .currentPlan(request.getCurrentPlan() != null ? request.getCurrentPlan() : "basic")
                .billing(request.getBilling() != null ? request.getBilling() : "Автоматическое списание")
                .role(role)
                .status(userStatus)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .passwordChangeRequired(false) // При создании через API пароль уже задан пользователем
                .build();

        user = userRepository.save(user);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .company(user.getCompany())
                .country(user.getCountry())
                .contact(user.getContact())
                .currentPlan(user.getCurrentPlan())
                .billing(user.getBilling())
                .status(user.getStatus().name())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Регистрация администратора или менеджера
     * Используется для первоначальной настройки системы
     */
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        log.info("Admin/Manager registration attempt: username={}, role={}", request.getUsername(), request.getRoleName());

        // Проверка на существование username
        if (userRepository.existsByUsername(request.getUsername())) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("username", new String[]{"Username already exists"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", errors));
        }

        // Проверка на существование email
        if (userRepository.existsByEmail(request.getEmail())) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("email", new String[]{"Email already exists"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", errors));
        }

        // Проверка, что роль существует и это admin или manager
        Role role = roleRepository.findByName(request.getRoleName())
                .orElse(null);

        if (role == null) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("roleName", new String[]{"Role not found. Available roles: admin, manager"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", errors));
        }

        if (!"admin".equals(request.getRoleName()) && !"manager".equals(request.getRoleName())) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("roleName", new String[]{"Only 'admin' or 'manager' roles are allowed for this endpoint"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", errors));
        }

        // Создаем пользователя
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .avatar(request.getAvatar())
                .company(request.getCompany())
                .country(request.getCountry())
                .contact(request.getContact())
                .currentPlan(request.getCurrentPlan() != null ? request.getCurrentPlan() : 
                        ("admin".equals(request.getRoleName()) ? "enterprise" : "company"))
                .billing(request.getBilling() != null ? request.getBilling() : "Автоматическое списание")
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .passwordChangeRequired(false) // При создании через API пароль уже задан пользователем
                .build();

        user = userRepository.save(user);

        log.info("✓ {} registered successfully: username={}, email={}", 
                request.getRoleName(), user.getUsername(), user.getEmail());

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .company(user.getCompany())
                .country(user.getCountry())
                .contact(user.getContact())
                .currentPlan(user.getCurrentPlan())
                .billing(user.getBilling())
                .status(user.getStatus().name())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
