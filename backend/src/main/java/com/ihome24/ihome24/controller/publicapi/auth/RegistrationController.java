package com.ihome24.ihome24.controller.publicapi.auth;

import com.ihome24.ihome24.dto.request.auth.AdminRegistrationRequest;
import com.ihome24.ihome24.dto.request.auth.RegisterRequest;
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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String normalizedPhone = normalizePhone(request.getPhone());
        if (normalizedPhone == null || normalizedPhone.length() < 10) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("phone", new String[]{"Введите корректный номер телефона"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
        }

        if (userRepository.existsByPhone(normalizedPhone)) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("phone", new String[]{"Этот номер телефона уже зарегистрирован"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
        }

        String email = request.getEmail() != null && !request.getEmail().isEmpty()
                ? request.getEmail().trim()
                : (normalizedPhone + "@ihome24.local");
        if (!email.endsWith("@ihome24.local") && userRepository.existsByEmail(email)) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("email", new String[]{"Эта почта уже привязана к другому аккаунту"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
        }

        Role role = roleRepository.findByName("users")
                .orElseThrow(() -> new RuntimeException("Default 'users' role not found"));

        String username = "user_" + normalizedPhone;

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .email(email)
                .fullName(request.getFullName())
                .phone(normalizedPhone)
                .currentPlan("basic")
                .billing("Автоматическое списание")
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .passwordChangeRequired(false)
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
                .phone(user.getPhone())
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

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9]", "");
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
                .phone(user.getPhone())
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
