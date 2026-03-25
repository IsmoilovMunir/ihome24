package com.ihome24.ihome24.controller.publicapi.auth;

import com.ihome24.ihome24.dto.response.auth.LoginResponse;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.entity.user.UserLoginDevice;
import com.ihome24.ihome24.repository.user.UserLoginDeviceRepository;
import com.ihome24.ihome24.service.auth.PhoneAuthService;
import com.ihome24.ihome24.service.auth.EmailTwoFactorService;
import com.ihome24.ihome24.service.auth.PasswordResetService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;

import com.ihome24.ihome24.config.security.JwtTokenService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneAuthService phoneAuthService;
    private final EmailTwoFactorService emailTwoFactorService;
    private final PasswordResetService passwordResetService;
    private final FileService fileService;
    private final UserLoginDeviceRepository userLoginDeviceRepository;
    private final JwtTokenService jwtTokenService;

    // Endpoint для JSON
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginJson(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        // Маскируем чувствительные данные в логах
        Map<String, String> safeRequest = new HashMap<>(request);
        if (safeRequest.containsKey("password")) {
            safeRequest.put("password", "***");
        }
        log.info("Login request (JSON): phone={}", safeRequest.get("phone"));
        return processLogin(request, httpRequest);
    }

    // Endpoint для form-urlencoded
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> loginForm(@RequestParam Map<String, String> request, HttpServletRequest httpRequest) {
        log.info("Login request (Form): phone={}", request.get("phone"));
        return processLogin(request, httpRequest);
    }

    private ResponseEntity<?> processLogin(Map<String, String> request, HttpServletRequest httpRequest) {
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

            // Reload user with role and permissions to avoid lazy-init issues when building JWT and abilities.
            user = userRepository.findByUsernameWithRoleAndPermissions(user.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Сохраняем информацию о входе (устройство, IP)
            try {
                String ip = extractClientIp(httpRequest);
                String ua = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
                UserLoginDevice device = UserLoginDevice.builder()
                        .user(user)
                        .ipAddress(ip)
                        .userAgent(ua)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();
                userLoginDeviceRepository.save(device);
            } catch (Exception e) {
                log.warn("Failed to save login device info: {}", e.getMessage());
            }

            // Email 2FA:
            // - если запрос пришёл с админ-панели (adminPanel=true) => требуем код для всех ролей
            // - иначе сохраняем старое поведение: только для ROLE_ADMIN
            String adminPanelFlag = request != null ? request.get("adminPanel") : null;
            boolean adminPanelLogin = adminPanelFlag != null && Boolean.parseBoolean(adminPanelFlag);
            boolean requireEmail2FA = adminPanelLogin
                    || (user.getRole() != null && "admin".equals(user.getRole().getName()));

            if (requireEmail2FA) {
                log.info("Starting email 2FA for user {} (adminPanelLogin={})", user.getUsername(), adminPanelLogin);
                Map<String, Object> twoFactor = emailTwoFactorService.startEmailTwoFactor(user);
                // Если отправка кода не удалась, twoFactorRequired будет false и вернем ошибку на фронт
                return ResponseEntity.ok(twoFactor);
            }

            return buildLoginResponse(user);
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
    public ResponseEntity<?> verifySmsCode(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
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

        saveLoginDevice(user, httpRequest);

        return buildLoginResponse(user);
    }

    /**
     * Подтверждение входа по коду из email для администраторов.
     * Шаг 2: после ввода логина и пароля.
     */
    @PostMapping("/verify-email-code")
    public ResponseEntity<?> verifyEmailCode(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String twoFactorToken = request != null ? request.get("twoFactorToken") : null;
        String code = request != null ? request.get("code") : null;

        Map<String, Object> result = emailTwoFactorService.verifyCode(twoFactorToken, code);
        if (!Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        Long userId = (Long) result.get("userId");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user = userRepository.findByUsernameWithRoleAndPermissions(user.getUsername()).orElse(user);

        saveLoginDevice(user, httpRequest);

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

    /**
     * Шаг 1: запрос сброса пароля (без авторизации).
     * Принимает email или username, отправляет письмо со ссылкой.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request != null ? request.get("email") : null;
        Map<String, Object> result = passwordResetService.requestReset(email);
        if (!Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Шаг 2: установка нового пароля по токену из письма.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request != null ? request.get("token") : null;
        String newPassword = request != null ? request.get("newPassword") : null;
        String confirmPassword = request != null ? request.get("confirmPassword") : null;

        Map<String, Object> result = passwordResetService.resetPassword(token, newPassword, confirmPassword);
        if (!Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Список последних устройств, с которых выполнялся вход в аккаунт.
     */
    @GetMapping("/login-devices")
    public ResponseEntity<?> getLoginDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var devices = userLoginDeviceRepository.findTop10ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(d -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("browser", parseBrowserName(d.getUserAgent()));
                    item.put("device", parseDeviceName(d.getUserAgent()));
                    item.put("location", formatLoginLocation(d.getIpAddress()));
                    item.put("recentActivity", formatLoginTime(d.getCreatedAt()));
                    item.put("deviceIcon", resolveDeviceIcon(d.getUserAgent()));
                    return item;
                })
                .toList();

        return ResponseEntity.ok(devices);
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

        // Generate JWT access token. Our User entity implements UserDetails and already exposes authorities.
        String accessToken = jwtTokenService.generateAccessToken(user);
        // Normalize role to avoid issues with casing/whitespace differences in DB (e.g. "EDITOR " vs "editor")
        String role = (String) userData.get("role");
        String normalizedRole = role != null ? role.toLowerCase().trim() : null;

        // CASL rules control visibility of menu/pages/buttons in admin UI.
        List<Map<String, Object>> userAbilityRules = new java.util.ArrayList<>();

        if ("admin".equals(normalizedRole)) {
            // Full access for admin.
            userAbilityRules.add(Map.of("action", "manage", "subject", "all"));
        } else {
            // Permission-driven abilities for menu visibility (CASL).
            var rolePermissions = user.getRole() != null ? user.getRole().getPermissions() : null;
            var permissionNames = rolePermissions == null
                    ? java.util.Set.<String>of()
                    : rolePermissions.stream()
                    .map(p -> p.getName())
                    .filter(java.util.Objects::nonNull)
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toSet());

            boolean hasContentManagement = permissionNames.contains("Управление контентом");
            boolean hasSeoManagement = permissionNames.contains("Управление SEO");
            boolean hasEcommerceManagement = permissionNames.contains("Отчетность")
                    || permissionNames.contains("Заработная плата");

            // Content management: full product editing + SEO editing capabilities.
            if (hasContentManagement) {
                userAbilityRules.add(Map.of("action", "read", "subject", "Ecommerce"));
                userAbilityRules.add(Map.of("action", "manage", "subject", "EcommerceProduct"));
                userAbilityRules.add(Map.of("action", "read", "subject", "EcommerceProduct"));

                userAbilityRules.add(Map.of("action", "manage", "subject", "EcommerceProductSEO"));
                userAbilityRules.add(Map.of("action", "read", "subject", "EcommerceProductSEO"));
            }

            // SEO-only: allow only SEO editing on product pages (no general product/category management).
            if (hasSeoManagement) {
                userAbilityRules.add(Map.of("action", "read", "subject", "Ecommerce"));
                userAbilityRules.add(Map.of("action", "manage", "subject", "EcommerceProductSEO"));
                userAbilityRules.add(Map.of("action", "read", "subject", "EcommerceProductSEO"));
            }

            // Orders/customers management.
            if (hasEcommerceManagement) {
                userAbilityRules.add(Map.of("action", "read", "subject", "Ecommerce"));
                userAbilityRules.add(Map.of("action", "manage", "subject", "Ecommerce"));
            }

            // User/Role management is admin-only in admin panel.
        }

        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .userData(userData)
                .userAbilityRules(userAbilityRules)
                .build();
        return ResponseEntity.ok(response);
    }

    private void saveLoginDevice(User user, HttpServletRequest request) {
        if (user == null || request == null) return;
        try {
            String ip = extractClientIp(request);
            String ua = request.getHeader("User-Agent");
            UserLoginDevice device = UserLoginDevice.builder()
                    .user(user)
                    .ipAddress(ip)
                    .userAgent(ua)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            userLoginDeviceRepository.save(device);
        } catch (Exception e) {
            log.warn("Failed to save login device info: {}", e.getMessage());
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String parseBrowserName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Неизвестный браузер";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome/") && !ua.contains("edg/")) return "Chrome";
        if (ua.contains("firefox/")) return "Firefox";
        if (ua.contains("safari/") && !ua.contains("chrome/")) return "Safari";
        return "Браузер";
    }

    private String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Устройство";
        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone")) return "iPhone";
        if (ua.contains("ipad")) return "iPad";
        if (ua.contains("android")) return "Android";
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os") || ua.contains("macintosh")) return "macOS";
        return "Устройство";
    }

    private Map<String, Object> resolveDeviceIcon(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("windows")) return Map.of("icon", "tabler-brand-windows", "color", "primary");
        if (ua.contains("iphone") || ua.contains("ipad")) return Map.of("icon", "tabler-device-mobile", "color", "error");
        if (ua.contains("android")) return Map.of("icon", "tabler-brand-android", "color", "success");
        if (ua.contains("mac os") || ua.contains("macintosh")) return Map.of("icon", "tabler-brand-apple", "color", "secondary");
        return Map.of("icon", "tabler-device-laptop", "color", "info");
    }

    private String formatLoginLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return "Неизвестно";
        String ip = ipAddress.trim();
        if ("127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "Локально";
        }
        return "IP: " + ip;
    }

    private String formatLoginTime(java.time.LocalDateTime createdAt) {
        if (createdAt == null) return "—";
        return createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
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
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                Map<String, Object> errors = new HashMap<>();
                errors.put("authentication", new String[]{"User not authenticated"});
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errors", errors));
            }

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
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;

        // 1) Primary path: authenticated via Spring Security
        User user = null;
        if (username != null)
            user = userRepository.findByUsernameWithRoleAndPermissions(username).orElse(null);

        // 2) Fallback path: parse our "token_<userId>_<timestamp>" bearer token (used by frontend)
        // This avoids 401 loops when SecurityContext isn't populated for this lightweight token.
        if (user == null && request != null)
            user = getUserFromBearerToken(request);

        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));

        // Make sure role permissions are loaded (needed for CASL ability rules).
        if (user.getUsername() != null) {
            user = userRepository.findByUsernameWithRoleAndPermissions(user.getUsername()).orElse(user);
        }

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
        userData.put("address", user.getAddress());
        userData.put("state", user.getState());
        userData.put("zip", user.getZip());
        userData.put("currentPlan", user.getCurrentPlan());
        userData.put("billing", user.getBilling());
        userData.put("role", user.getRole() != null ? user.getRole().getName() : null);
        userData.put("status", user.getStatus() != null ? user.getStatus().name() : "ACTIVE");
        userData.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        userData.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);

        // Also return CASL rules so admin UI can immediately reflect role changes
        // without requiring re-login.
        String role = user.getRole() != null ? user.getRole().getName() : null;
        String normalizedRole = role != null ? role.toLowerCase().trim() : null;

        List<Map<String, Object>> userAbilityRules = new java.util.ArrayList<>();
        if ("admin".equals(normalizedRole)) {
            userAbilityRules.add(Map.of("action", "manage", "subject", "all"));
        } else {
            var rolePermissions = user.getRole() != null ? user.getRole().getPermissions() : null;
            var permissionNames = rolePermissions == null
                    ? java.util.Set.<String>of()
                    : rolePermissions.stream()
                    .map(p -> p.getName())
                    .filter(java.util.Objects::nonNull)
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toSet());

            boolean hasContentManagement = permissionNames.contains("Управление контентом");
            boolean hasSeoManagement = permissionNames.contains("Управление SEO");
            boolean hasEcommerceManagement = permissionNames.contains("Отчетность")
                    || permissionNames.contains("Заработная плата");

            if (hasContentManagement) {
                userAbilityRules.add(Map.of("action", "read", "subject", "Ecommerce"));
                userAbilityRules.add(Map.of("action", "manage", "subject", "EcommerceProduct"));
                userAbilityRules.add(Map.of("action", "read", "subject", "EcommerceProduct"));
                userAbilityRules.add(Map.of("action", "manage", "subject", "EcommerceProductSEO"));
                userAbilityRules.add(Map.of("action", "read", "subject", "EcommerceProductSEO"));
            }

            if (hasSeoManagement) {
                userAbilityRules.add(Map.of("action", "read", "subject", "Ecommerce"));
                userAbilityRules.add(Map.of("action", "manage", "subject", "EcommerceProductSEO"));
                userAbilityRules.add(Map.of("action", "read", "subject", "EcommerceProductSEO"));
            }

            if (hasEcommerceManagement) {
                userAbilityRules.add(Map.of("action", "read", "subject", "Ecommerce"));
                userAbilityRules.add(Map.of("action", "manage", "subject", "Ecommerce"));
            }

            // User/Role management is admin-only in admin panel.
        }

        userData.put("userAbilityRules", userAbilityRules);
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
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

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
        if (request.containsKey("address")) {
            user.setAddress((String) request.get("address"));
        }
        if (request.containsKey("state")) {
            user.setState((String) request.get("state"));
        }
        if (request.containsKey("zip")) {
            user.setZip((String) request.get("zip"));
        }
        if (request.containsKey("avatar")) {
            String avatar = (String) request.get("avatar");
            if (avatar != null && avatar.length() > 512) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("errors", Map.of("avatar", new String[]{"Слишком большой аватар. Используйте загрузку файла."})));
            }
            user.setAvatar(avatar);
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
        userData.put("address", user.getAddress());
        userData.put("state", user.getState());
        userData.put("zip", user.getZip());
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
        User user = getUserFromBearerToken(request);
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

    /**
     * Защита от неверного типа контента при загрузке аватара.
     * Если фронтенд по ошибке отправит JSON вместо multipart/form-data,
     * вернём понятную ошибку, а не HttpMediaTypeNotSupportedException.
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadAvatarJson(@RequestBody Map<String, Object> body) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "errors",
                        Map.of("avatar", new String[]{
                                "Загрузка аватара через JSON не поддерживается. " +
                                "Используйте обычную загрузку файла (multipart/form-data)."
                        })
                ));
    }

    /** Получить пользователя по токену (token_userId_timestamp). Не зависит от SecurityContext. */
    private User getUserFromBearerToken(HttpServletRequest request) {
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
