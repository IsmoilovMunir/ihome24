package com.ihome24.ihome24.config;

import com.ihome24.ihome24.service.telegram.TelegramBotHandler;
import com.ihome24.ihome24.service.telegram.TelegramBotService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Long polling для Telegram (локально и когда webhook не задан).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramPollingRunner {

    private final TelegramBotService telegramBotService;
    private final TelegramBotHandler telegramBotHandler;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicLong offset = new AtomicLong(0);
    private Thread pollingThread;

    @Value("${app.telegram.polling-enabled:false}")
    private boolean pollingEnabled;

    @Value("${app.telegram.webhook-url:}")
    private String webhookUrl;

    @PostConstruct
    void start() {
        if (!telegramBotService.isConfigured()) {
            log.warn("Telegram bot token not set — polling skipped");
            return;
        }

        boolean hasWebhook = webhookUrl != null && !webhookUrl.isBlank();
        if (hasWebhook) {
            log.info("Telegram webhook URL configured — polling disabled");
            return;
        }

        if (!pollingEnabled) {
            log.info("Telegram polling disabled (app.telegram.polling-enabled=false)");
            return;
        }

        try {
            telegramBotService.deleteWebhook();
            pollingThread = new Thread(this::pollLoop, "telegram-polling");
            pollingThread.setDaemon(true);
            pollingThread.start();
            log.info("Telegram polling started for @ihome24bot");
        } catch (Exception e) {
            log.warn("Telegram polling not started: {} — backend will run without bot updates", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void pollLoop() {
        while (running.get()) {
            try {
                Map<String, Object> response = telegramBotService.getUpdates(offset.get(), 25);
                if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
                    sleepQuiet(3000);
                    continue;
                }

                List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");
                if (updates == null || updates.isEmpty()) {
                    continue;
                }

                for (Map<String, Object> update : updates) {
                    Object updateId = update.get("update_id");
                    if (updateId instanceof Number num) {
                        offset.set(num.longValue() + 1);
                    }
                    try {
                        telegramBotHandler.handleUpdate(update);
                    } catch (Exception e) {
                        log.error("Telegram update handler failed: {}", e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.warn("Telegram polling error: {}", e.getMessage());
                sleepQuiet(5000);
            }
        }
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    void stop() {
        running.set(false);
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }
}
