package com.ihome24.ihome24.service.auth;

import com.ihome24.ihome24.entity.user.PasswordResetToken;
import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.repository.user.PasswordResetTokenRepository;
import com.ihome24.ihome24.repository.user.UserRepository;
import com.ihome24.ihome24.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> requestReset(String emailOrUsername) {
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            return error("email", "Укажите email или имя пользователя");
        }

        String value = emailOrUsername.trim();
        Optional<User> userOpt = userRepository.findByEmail(value);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(value);
        }

        if (userOpt.isEmpty()) {
            return error("email", "Пользователь с таким email или именем не найден");
        }

        User user = userOpt.get();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return error("email", "Для этого пользователя не указан email. Обратитесь к администратору.");
        }

        // помечаем старые токены как использованные
        tokenRepository.markAllAsUsedForEmail(user.getEmail());

        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken entity = PasswordResetToken.builder()
                .token(token)
                .email(user.getEmail())
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES))
                .used(false)
                .build();
        tokenRepository.save(entity);

        try {
            emailService.sendPasswordResetLink(user.getEmail(), token);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
            return error("email", "Не удалось отправить письмо для сброса пароля. Попробуйте позже.");
        }

        return Map.of("success", true);
    }

    @Transactional
    public Map<String, Object> resetPassword(String token, String newPassword, String confirmPassword) {
        if (token == null || token.isBlank()) {
            return error("token", "Неверная или истекшая ссылка для сброса");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return error("newPassword", "Введите новый пароль");
        }
        if (newPassword.length() < 6) {
            return error("newPassword", "Пароль должен быть не менее 6 символов");
        }
        if (!newPassword.equals(confirmPassword)) {
            return error("confirmPassword", "Пароли не совпадают");
        }

        Optional<PasswordResetToken> tokenOpt =
                tokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(token, LocalDateTime.now());
        if (tokenOpt.isEmpty()) {
            return error("token", "Неверная или истекшая ссылка для сброса");
        }

        PasswordResetToken prt = tokenOpt.get();
        Optional<User> userOpt = userRepository.findByEmail(prt.getEmail());
        if (userOpt.isEmpty()) {
            return error("token", "Пользователь для этой ссылки не найден");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(false);
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);

        log.info("Password reset successfully for user {}", user.getUsername());
        return Map.of("success", true);
    }

    private Map<String, Object> error(String field, String message) {
        return Map.of(
                "success", false,
                "errors", Map.of(field, new String[]{message})
        );
    }
}

