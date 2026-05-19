package com.ihome24.ihome24.config;

import com.ihome24.ihome24.service.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.telegram.webhook-url")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookRegistrar {

    private final TelegramBotService telegramBotService;

    @Value("${app.telegram.webhook-url:}")
    private String webhookUrl;

    @Value("${app.telegram.webhook-secret:}")
    private String webhookSecret;

    @EventListener(ApplicationReadyEvent.class)
    public void registerWebhook() {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        if (!telegramBotService.isConfigured()) {
            log.warn("Telegram webhook URL set but bot token missing");
            return;
        }
        telegramBotService.setWebhook(webhookUrl.trim(), webhookSecret);
    }
}
