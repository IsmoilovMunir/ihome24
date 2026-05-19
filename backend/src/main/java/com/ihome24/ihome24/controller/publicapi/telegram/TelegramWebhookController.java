package com.ihome24.ihome24.controller.publicapi.telegram;

import com.ihome24.ihome24.service.telegram.TelegramBotHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/publicapi/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private final TelegramBotHandler telegramBotHandler;

    @Value("${app.telegram.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody Map<String, Object> update,
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secretHeader) {

        if (webhookSecret != null && !webhookSecret.isBlank()) {
            if (secretHeader == null || !webhookSecret.trim().equals(secretHeader)) {
                log.warn("Telegram webhook rejected: invalid secret token");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        try {
            telegramBotHandler.handleUpdate(update);
        } catch (Exception e) {
            log.error("Telegram webhook handler error: {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
