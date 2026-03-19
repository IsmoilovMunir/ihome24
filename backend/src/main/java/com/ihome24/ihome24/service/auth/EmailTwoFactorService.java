package com.ihome24.ihome24.service.auth;

import com.ihome24.ihome24.entity.user.User;
import com.ihome24.ihome24.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTwoFactorService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_TTL_MINUTES = 15;

    private final EmailService emailService;

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public Map<String, Object> startEmailTwoFactor(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Email 2FA requested for user {} but email is empty", user.getUsername());
            return Map.of(
                    "twoFactorRequired", false,
                    "error", "У администратора не указан email. Обратитесь к разработчику."
            );
        }

        String code = generateCode();
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES);

        sessions.put(token, new Session(user.getId(), code, expiresAt));

        try {
            emailService.sendVerificationCode(user.getEmail(), code, null);
        } catch (Exception e) {
            log.error("Failed to send email 2FA code to {}: {}", user.getEmail(), e.getMessage());
            sessions.remove(token);
            return Map.of(
                    "twoFactorRequired", false,
                    "error", "Не удалось отправить код на email. Попробуйте позже или свяжитесь с поддержкой."
            );
        }

        return Map.of(
                "twoFactorRequired", true,
                "twoFactorToken", token
        );
    }

    public Map<String, Object> verifyCode(String twoFactorToken, String code) {
        if (twoFactorToken == null || twoFactorToken.isBlank()) {
            return error("twoFactorToken", "Некорректный токен сессии");
        }
        if (code == null || code.trim().length() != CODE_LENGTH) {
            return error("code", "Введите " + CODE_LENGTH + "-значный код");
        }

        Session session = sessions.get(twoFactorToken);
        if (session == null) {
            return error("twoFactorToken", "Сессия подтверждения не найдена или истекла");
        }

        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            sessions.remove(twoFactorToken);
            return error("code", "Код истёк. Начните вход заново.");
        }

        if (!session.code.equals(code.trim())) {
            return error("code", "Неверный код");
        }

        sessions.remove(twoFactorToken);

        return Map.of(
                "success", true,
                "userId", session.userId
        );
    }

    private String generateCode() {
        int min = (int) Math.pow(10, CODE_LENGTH - 1);
        int max = (int) Math.pow(10, CODE_LENGTH) - 1;
        int value = min + RANDOM.nextInt(max - min + 1);
        return String.format("%0" + CODE_LENGTH + "d", value);
    }

    private Map<String, Object> error(String field, String message) {
        return Map.of(
                "success", false,
                "errors", Map.of(field, new String[]{message})
        );
    }

    private record Session(Long userId, String code, LocalDateTime expiresAt) {
    }
}

