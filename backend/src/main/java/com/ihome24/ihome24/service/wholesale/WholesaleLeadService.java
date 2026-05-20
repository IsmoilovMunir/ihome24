package com.ihome24.ihome24.service.wholesale;

import com.ihome24.ihome24.dto.request.wholesale.WholesaleLeadRequest;
import com.ihome24.ihome24.entity.wholesale.WholesaleLead;
import com.ihome24.ihome24.repository.wholesale.WholesaleLeadRepository;
import com.ihome24.ihome24.service.email.EmailService;
import com.ihome24.ihome24.service.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WholesaleLeadService {

    /** SMTP на VPS часто долго таймаутит — не блокируем HTTP, если Telegram уже ушёл. */
    private static final ExecutorService EMAIL_EXECUTOR = Executors.newCachedThreadPool();
    private static final int EMAIL_WAIT_SECONDS = 12;
    private static final int TELEGRAM_WAIT_SECONDS = 12;

    private final EmailService emailService;
    private final TelegramBotService telegramBotService;
    private final WholesaleLeadRepository wholesaleLeadRepository;

    @Value("${app.wholesale.email:opt@ihome24.ru}")
    private String wholesaleEmail;

    @Transactional
    public Result submitLead(WholesaleLeadRequest request) {
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            log.info("Wholesale lead honeypot triggered, ignoring");
            return new Result(true, true, true);
        }

        String name = trim(request.getName());
        String phone = trim(request.getPhone());
        String inn = trim(request.getInn());
        String message = trim(request.getMessage());

        wholesaleLeadRepository.save(WholesaleLead.builder()
                .name(name)
                .phone(phone)
                .inn(inn)
                .message(message)
                .build());

        String telegramText = formatTelegramMessage(name, phone, inn, message);

        boolean emailSent = sendEmailWithTimeout(name, phone, inn, message);
        boolean telegramSent;
        if (emailSent) {
            scheduleTelegramInBackground(telegramText);
            telegramSent = false;
        } else {
            telegramSent = sendTelegramWithTimeout(telegramText);
        }

        if (!emailSent && !telegramSent) {
            log.error("Wholesale lead failed on all channels (email + telegram)");
            throw new IllegalStateException(
                    "Не удалось отправить заявку. Позвоните нам или напишите на opt@ihome24.ru");
        }

        if (emailSent && telegramSent) {
            log.info("Wholesale lead submitted to email and telegram");
        } else if (emailSent) {
            log.warn("Wholesale lead submitted by email only (telegram unavailable)");
        } else {
            log.warn("Wholesale lead submitted by telegram only (email unavailable)");
        }

        return new Result(true, emailSent, telegramSent);
    }

    /** Не блокируем HTTP ожиданием Telegram (таймауты сети до api.telegram.org). */
    private boolean scheduleTelegramInBackground(String text) {
        if (!telegramBotService.isConfigured()) {
            return false;
        }
        EMAIL_EXECUTOR.execute(() -> sendTelegramSafe(text));
        return false;
    }

    private boolean sendEmailWithTimeout(String name, String phone, String inn, String message) {
        try {
            return EMAIL_EXECUTOR
                    .submit(() -> sendEmailSafe(name, phone, inn, message))
                    .get(EMAIL_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Wholesale email timed out after {}s", EMAIL_WAIT_SECONDS);
            return false;
        } catch (Exception e) {
            log.warn("Wholesale email channel error: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendEmailSafe(String name, String phone, String inn, String message) {
        try {
            return emailService.sendWholesaleLead(wholesaleEmail, name, phone, inn, message);
        } catch (Exception e) {
            log.warn("Wholesale email channel error: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendTelegramWithTimeout(String text) {
        if (!telegramBotService.isConfigured()) {
            return false;
        }
        try {
            return EMAIL_EXECUTOR
                    .submit(() -> sendTelegramSafe(text))
                    .get(TELEGRAM_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Wholesale telegram timed out after {}s", TELEGRAM_WAIT_SECONDS);
            return false;
        } catch (Exception e) {
            log.warn("Wholesale telegram channel error: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendTelegramSafe(String text) {
        try {
            return telegramBotService.sendMessage(text);
        } catch (Exception e) {
            log.warn("Wholesale telegram channel error: {}", e.getMessage());
            return false;
        }
    }

    private static String formatTelegramMessage(String name, String phone, String inn, String message) {
        StringBuilder sb = new StringBuilder("🛒 Новая заявка: оптовым клиентам iHome24\n\n");
        sb.append("Имя/компания: ").append(name).append('\n');
        sb.append("Телефон: ").append(phone).append('\n');
        if (inn != null && !inn.isBlank()) {
            sb.append("ИНН: ").append(inn).append('\n');
        }
        if (message != null && !message.isBlank()) {
            sb.append("\nКомментарий:\n").append(message);
        }
        return sb.toString();
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    public record Result(boolean success, boolean emailSent, boolean telegramSent) {}
}
