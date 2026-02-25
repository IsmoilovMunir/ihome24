package com.ihome24.ihome24.service.auth;

import com.ihome24.ihome24.entity.user.PhoneAuthSession;
import com.ihome24.ihome24.entity.user.PhoneVerificationCode;
import com.ihome24.ihome24.entity.user.Role;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.*;
import com.ihome24.ihome24.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneAuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RATE_LIMIT_SECONDS = 60; // 1 минута между запросами (для тестирования)
    private static final int MAX_CODES_PER_PHONE_PER_HOUR = 10;

    private final PhoneVerificationCodeRepository phoneVerificationCodeRepository;
    private final PhoneAuthSessionRepository phoneAuthSessionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    public String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9]", "");
    }

    /**
     * Отправить SMS с кодом на номер телефона.
     */
    @Transactional
    public Map<String, Object> sendCode(String phone) {
        String normalized = normalizePhone(phone);
        log.info("sendCode: phone={}, normalized={}", phone, normalized);
        if (normalized == null || normalized.length() < 10) {
            return error("phone", "Введите корректный номер телефона (минимум 10 цифр)");
        }

        LocalDateTime since = LocalDateTime.now().minusMinutes(60);
        long count = phoneVerificationCodeRepository.countByPhoneSince(normalized, since);
        if (count >= MAX_CODES_PER_PHONE_PER_HOUR) {
            return error("phone", "Превышен лимит запросов. Попробуйте позже.");
        }

        LocalDateTime rateLimitSince = LocalDateTime.now().minusSeconds(RATE_LIMIT_SECONDS);
        long recentCount = phoneVerificationCodeRepository.countByPhoneSince(normalized, rateLimitSince);
        if (recentCount > 0) {
            return error("phone", "Код уже отправлен. Подождите 1 минуту.");
        }

        String code = String.format("%06d", 100000 + RANDOM.nextInt(900000));
        phoneVerificationCodeRepository.markAllAsUsedForPhone(normalized);

        PhoneVerificationCode entity = PhoneVerificationCode.builder()
                .phone(normalized)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        phoneVerificationCodeRepository.save(entity);

        if (!smsService.sendVerificationCode(normalized, code)) {
            return error("phone", "Не удалось отправить SMS. Попробуйте позже.");
        }

        return Map.of("success", true, "message", "Код отправлен на указанный номер");
    }

    /**
     * Проверить код. Если пользователь существует — возвращает auth. Если нет — needsRegistration + registrationToken.
     */
    @Transactional
    public Map<String, Object> verifyCode(String phone, String code) {
        String normalized = normalizePhone(phone);
        if (normalized == null || normalized.length() < 10) {
            return error("phone", "Введите корректный номер телефона");
        }
        if (code == null || code.trim().length() != 6) {
            return error("code", "Введите 6-значный код");
        }

        Optional<PhoneVerificationCode> opt = phoneVerificationCodeRepository
                .findTopByPhoneAndCodeAndUsedFalseOrderByCreatedAtDesc(normalized, code.trim());

        if (opt.isEmpty()) {
            return error("code", "Неверный или истёкший код");
        }

        PhoneVerificationCode vc = opt.get();
        if (!vc.isValid()) {
            return error("code", "Код истёк. Запросите новый.");
        }

        vc.setUsed(true);
        phoneVerificationCodeRepository.save(vc);

        Optional<User> userOpt = userRepository.findByPhone(normalized);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isEnabled() || user.getStatus() != User.UserStatus.ACTIVE) {
                return error("phone", "Аккаунт отключен или неактивен");
            }
            return Map.of(
                    "success", true,
                    "needsRegistration", false,
                    "userId", user.getId()
            );
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        PhoneAuthSession session = PhoneAuthSession.builder()
                .token(token)
                .phone(normalized)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        phoneAuthSessionRepository.save(session);

        return Map.of(
                "success", true,
                "needsRegistration", true,
                "registrationToken", token
        );
    }

    /**
     * Завершить регистрацию: ФИО, email. Требует registrationToken от verifyCode.
     */
    @Transactional
    public Map<String, Object> completeRegistration(String registrationToken, String fullName, String email) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return error("fullName", "Укажите ФИО");
        }

        Optional<PhoneAuthSession> sessionOpt = phoneAuthSessionRepository
                .findByTokenAndExpiresAtAfter(registrationToken, LocalDateTime.now());

        if (sessionOpt.isEmpty()) {
            return error("registrationToken", "Сессия истекла. Начните вход заново.");
        }

        PhoneAuthSession session = sessionOpt.get();
        String phone = session.getPhone();

        if (userRepository.existsByPhone(phone)) {
            phoneAuthSessionRepository.delete(session);
            return error("phone", "Пользователь уже зарегистрирован");
        }

        String userEmail = (email != null && !email.trim().isEmpty())
                ? email.trim()
                : (phone + "@ihome24.local");
        if (!userEmail.endsWith("@ihome24.local") && userRepository.existsByEmail(userEmail)) {
            return error("email", "Эта почта уже привязана к другому аккаунту");
        }

        Role role = roleRepository.findByName("users")
                .orElseThrow(() -> new RuntimeException("Role 'users' not found"));

        String username = "user_" + phone;

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(userEmail)
                .fullName(fullName.trim())
                .phone(phone)
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
        phoneAuthSessionRepository.delete(session);

        return Map.of(
                "success", true,
                "userId", user.getId()
        );
    }

    private Map<String, Object> error(String field, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("errors", Map.of(field, new String[]{message}));
        return result;
    }
}
