package com.ihome24.ihome24.controller.publicapi.auth;

import com.ihome24.ihome24.dto.response.auth.LoginResponse;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.service.auth.PhoneAuthService;
import com.ihome24.ihome24.service.storage.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneAuthService phoneAuthService;
    private final FileService fileService;

    // Endpoint для JSON
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginJson(@RequestBody Map<String, String> request) {
        // Маскируем чувствительные данные в логах
        Map<String, String> safeRequest = new HashMap<>(request);
        if (safeRequest.containsKey("password")) {
            safeRequest.put("password", "***");
        }
        log.info("Login request (JSON): phone={}", safeRequest.get("phone"));
        return processLogin(request);
    }

    // Endpoint для form-urlencoded
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> loginForm(@RequestParam Map<String, String> request) {
        log.info("Login request (Form): phone={}", request.get("phone"));
        return processLogin(request);
    }

    private ResponseEntity<?> processLogin(Map<String, String> request) {
        try {
            // Авторизация по номеру телефона (приоритет), email или username
            String phone = request.get("phone");
            String email = request.get("email");
            String username = request.get("username");
            String password = request.get("password");
            
            if (password == null) {
                Map<String, Object> errors = new HashMap<>();
                errors.put("password", new String[]{"Пароль обязателен"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            User user = null;
            String searchValue = null;
            String errorField = "phone";
            
            if (phone != null && !phone.isEmpty()) {
                searchValue = normalizePhone(phone);
                user = userRepository.findByPhoneWithRoleAndPermissions(searchValue).orElse(null);
                errorField = "phone";
            } else if (email != null && !email.isEmpty()) {
                searchValue = email;
                user = userRepository.findByEmail(email).orElse(null);
                if (user == null) user = userRepository.findByUsername(email).orElse(null);
                errorField = "email";
            } else if (username != null && !username.isEmpty()) {
                searchValue = username;
                user = userRepository.findByUsername(username).orElse(null);
                if (user == null) user = userRepository.findByEmail(username).orElse(null);
                if (user == null) user = userRepository.findByPhoneWithRoleAndPermissions(username).orElse(null);
                errorField = "username";
            }
            
            if (searchValue == null) {
                Map<String, Object> errors = new HashMap<>();
                errors.put("phone", new String[]{"Номер телефона обязателен"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            if (user == null) {
                log.warn("Login attempt failed: User not found with phone/email/username: {}", searchValue);
                Map<String, Object> errors = new HashMap<>();
                errors.put(errorField, new String[]{"Неверный номер телефона или пароль"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            log.info("Found user: username={}, email={}, enabled={}, status={}", 
                    user.getUsername(), user.getEmail(), user.isEnabled(), user.getStatus());

            // Проверяем, что пользователь активен
            if (!user.isEnabled() || user.getStatus() != User.UserStatus.ACTIVE) {
                log.warn("Login attempt failed: User is not active. username={}, enabled={}, status={}", 
                        user.getUsername(), user.isEnabled(), user.getStatus());
                Map<String, Object> errors = new HashMap<>();
                errors.put(errorField, new String[]{"Аккаунт отключен или неактивен"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            // Аутентификация через Spring Security использует username
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(), // Spring Security использует username
                                password
                        )
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authentication successful for user: {}", user.getUsername());

            } catch (BadCredentialsException e) {
                log.warn("Login attempt failed: Bad credentials for user: {}", user.getUsername());
                Map<String, Object> errors = new HashMap<>();
                errors.put(errorField, new String[]{"Неверный номер телефона или пароль"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            } catch (AuthenticationException e) {
                log.error("Authentication failed for user: {}, error: {}", user.getUsername(), e.getMessage());
                Map<String, Object> errors = new HashMap<>();
                errors.put(errorField, new String[]{"Ошибка аутентификации: " + e.getMessage()});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            // Получаем пользователя с ролями и правами
            user = userRepository.findByUsernameWithRoleAndPermissions(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Формируем ответ в формате, ожидаемом фронтендом
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

            // Проверяем, требуется ли смена пароля
            if (Boolean.TRUE.equals(user.getPasswordChangeRequired())) {
                log.info("User {} requires password change", user.getUsername());
            }

            // Создаем простой токен (в продакшене лучше использовать JWT)
            String accessToken = "token_" + user.getId() + "_" + System.currentTimeMillis();

            // Формируем правила доступа (ability rules) для CASL
            Map<String, Object> abilityRules = new HashMap<>();
            
            // Если пользователь администратор, даем полный доступ
            if ("admin".equals(userData.get("role"))) {
                abilityRules.put("action", "manage");
                abilityRules.put("subject", "all");
            } else {
                // Для других ролей можно добавить специфичные правила
                abilityRules.put("action", "read");
                abilityRules.put("subject", "AclDemo");
            }
            
            List<Map<String, Object>> userAbilityRules = new ArrayList<>();
            userAbilityRules.add(abilityRules);

            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .userData(userData)
                    .userAbilityRules(userAbilityRules)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error during login: ", e);
            Map<String, Object> errors = new HashMap<>();
            errors.put("phone", new String[]{"Неверный номер телефона или пароль"});
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
        }
    }

    /** Нормализация номера телефона: оставляем только цифры */
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9]", "");
    }

    /** Вход по SMS: отправить код на номер */
    @PostMapping("/send-sms-code")
    public ResponseEntity<?> sendSmsCode(@RequestBody Map<String, String> request) {
        String phone = request != null ? request.get("phone") : null;
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errors", Map.of("phone", new String[]{"Введите номер телефона"})));
        }
        Map<String, Object> result = phoneAuthService.sendCode(phone);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /** Вход по SMS: проверить код. Возвращает accessToken или needsRegistration + registrationToken */
    @PostMapping("/verify-sms-code")
    public ResponseEntity<?> verifySmsCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");
        Map<String, Object> result = phoneAuthService.verifyCode(phone, code);
        if (!Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        if (Boolean.TRUE.equals(result.get("needsRegistration"))) {
            return ResponseEntity.ok(Map.of(
                    "needsRegistration", true,
                    "registrationToken", result.get("registrationToken")
            ));
        }
        Long userId = (Long) result.get("userId");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user = userRepository.findByUsernameWithRoleAndPermissions(user.getUsername()).orElse(user);
        return buildLoginResponse(user);
    }

    /** Завершить регистрацию после верификации телефона: ФИО, email */
    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeRegistration(@RequestBody Map<String, String> request) {
        String token = request.get("registrationToken");
        String fullName = request.get("fullName");
        String email = request.get("email");
        Map<String, Object> result = phoneAuthService.completeRegistration(token, fullName, email);
        if (!Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        Long userId = (Long) result.get("userId");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user = userRepository.findByUsernameWithRoleAndPermissions(user.getUsername()).orElse(user);
        return buildLoginResponse(user);
    }

    private ResponseEntity<?> buildLoginResponse(User user) {
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

        String accessToken = "token_" + user.getId() + "_" + System.currentTimeMillis();
        Map<String, Object> abilityRules = new HashMap<>();
        abilityRules.put("action", "admin".equals(userData.get("role")) ? "manage" : "read");
        abilityRules.put("subject", "admin".equals(userData.get("role")) ? "all" : "AclDemo");
        List<Map<String, Object>> userAbilityRules = List.of(abilityRules);

        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .userData(userData)
                .userAbilityRules(userAbilityRules)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Endpoint для смены пароля
     * Используется когда требуется принудительная смена пароля
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            if (newPassword == null || newPassword.isEmpty()) {
                Map<String, Object> errors = new HashMap<>();
                errors.put("newPassword", new String[]{"New password is required"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            if (newPassword.length() < 6) {
                Map<String, Object> errors = new HashMap<>();
                errors.put("newPassword", new String[]{"Password must be at least 6 characters"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            if (!newPassword.equals(confirmPassword)) {
                Map<String, Object> errors = new HashMap<>();
                errors.put("confirmPassword", new String[]{"Passwords do not match"});
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
            }

            // Получаем текущего пользователя из SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> errors = new HashMap<>();
                errors.put("authentication", new String[]{"User not authenticated"});
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errors", errors));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Проверяем текущий пароль, если требуется
            if (currentPassword != null && !currentPassword.isEmpty()) {
                try {
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(username, currentPassword)
                    );
                } catch (BadCredentialsException e) {
                    Map<String, Object> errors = new HashMap<>();
                    errors.put("currentPassword", new String[]{"Current password is incorrect"});
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", errors));
                }
            }

            // Обновляем пароль
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordChangeRequired(false); // Снимаем флаг обязательной смены пароля
            userRepository.save(user);

            log.info("Password changed successfully for user: {}", username);

            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully",
                    "success", true
            ));
        } catch (Exception e) {
            log.error("Error changing password: ", e);
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", new String[]{"Failed to change password: " + e.getMessage()});
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errors", errors));
        }
    }

    /**
     * Endpoint для получения текущего пользователя
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsernameWithRoleAndPermissions(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("fullName", user.getFullName());
        userData.put("avatar", user.getAvatar());
        userData.put("company", user.getCompany());
        userData.put("country", user.getCountry());
        userData.put("contact", user.getContact());
        userData.put("currentPlan", user.getCurrentPlan());
        userData.put("billing", user.getBilling());
        userData.put("role", user.getRole() != null ? user.getRole().getName() : null);
        userData.put("status", user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
        userData.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        userData.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);

        return ResponseEntity.ok(userData);
    }

    /**
     * Endpoint для обновления текущего пользователя
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Обновляем поля пользователя
        if (request.containsKey("fullName")) {
            user.setFullName((String) request.get("fullName"));
        }
        if (request.containsKey("email")) {
            String newEmail = request.get("email") != null ? ((String) request.get("email")).trim() : null;
            if (newEmail != null && !newEmail.isEmpty()) {
                if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("errors", Map.of("email", new String[]{"Эта почта уже привязана к другому аккаунту"})));
                }
            }
            user.setEmail(newEmail);
        }
        if (request.containsKey("phone")) {
            String newPhone = request.get("phone") != null ? normalizePhone((String) request.get("phone")) : null;
            if (newPhone != null && !newPhone.isEmpty()) {
                if (userRepository.existsByPhoneAndIdNot(newPhone, user.getId())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("errors", Map.of("phone", new String[]{"Этот номер уже привязан к другому аккаунту"})));
                }
            }
            user.setPhone(newPhone);
        }
        if (request.containsKey("company")) {
            user.setCompany((String) request.get("company"));
        }
        if (request.containsKey("country")) {
            user.setCountry((String) request.get("country"));
        }
        if (request.containsKey("contact")) {
            user.setContact((String) request.get("contact"));
        }
        if (request.containsKey("avatar")) {
            user.setAvatar((String) request.get("avatar"));
        }

        user = userRepository.save(user);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("avatar", user.getAvatar());
        userData.put("company", user.getCompany());
        userData.put("country", user.getCountry());
        userData.put("contact", user.getContact());
        userData.put("currentPlan", user.getCurrentPlan());
        userData.put("billing", user.getBilling());
        userData.put("role", user.getRole() != null ? user.getRole().getName() : null);
        userData.put("status", user.getStatus() != null ? user.getStatus().name() : "ACTIVE");

        return ResponseEntity.ok(userData);
    }

    /**
     * Загрузка аватара текущего пользователя
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is required. Use form field 'file'."));
        }

        try {
            String avatarPath = fileService.uploadAvatar(file, user.getId());
            user.setAvatar(avatarPath);
            user = userRepository.save(user);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());
            userData.put("avatar", user.getAvatar());
            userData.put("company", user.getCompany());
            userData.put("country", user.getCountry());
            userData.put("contact", user.getContact());
            userData.put("currentPlan", user.getCurrentPlan());
            userData.put("billing", user.getBilling());
            userData.put("role", user.getRole() != null ? user.getRole().getName() : null);
            userData.put("status", user.getStatus() != null ? user.getStatus().name() : "ACTIVE");

            return ResponseEntity.ok(userData);
        } catch (IOException e) {
            log.error("Failed to upload avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload avatar: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error uploading avatar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /** Получить текущего пользователя по токену (token_userId_timestamp). Не зависит от SecurityContext. */
    private User getCurrentUser(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer == null || !bearer.startsWith("Bearer ")) return null;
        String token = bearer.substring(7);
        if (token == null || !token.startsWith("token_")) return null;
        String[] parts = token.split("_");
        if (parts.length < 2) return null;
        try {
            Long userId = Long.parseLong(parts[1]);
            return userRepository.findById(userId).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Endpoint для проверки существующих пользователей (только для отладки)
     */
    @GetMapping("/check-users")
    public ResponseEntity<?> checkUsers() {
        long totalUsers = userRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", totalUsers);
        response.put("message", "Admin user is created automatically on first startup");
        response.put("config", Map.of(
                "adminUsername", "admin (configurable via app.admin.username)",
                "adminEmail", "ismoilov.munir97@gmail.com (configurable via app.admin.email)",
                "passwordChangeRequired", "true (configurable via app.admin.passwordChangeRequired)"
        ));
        return ResponseEntity.ok(response);
    }

}
