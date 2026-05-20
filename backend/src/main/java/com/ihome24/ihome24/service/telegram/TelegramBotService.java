package com.ihome24.ihome24.service.telegram;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TelegramBotService {

    public static final String BTN_LEADS = "📋 Заявки";
    public static final String BTN_ORDERS = "📦 Заказы";

    @Value("${app.telegram.bot-token:}")
    private String botToken;

    @Value("${app.telegram.chat-id:}")
    private String chatId;

    /** Иначе при блокировке api.telegram.org на VPS запросы висят минутами. */
    @Value("${app.telegram.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${app.telegram.read-timeout-ms:10000}")
    private int readTimeoutMs;

    private RestTemplate restTemplate;

    @PostConstruct
    void initRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        restTemplate = new RestTemplate(factory);
    }

    public boolean isConfigured() {
        return botToken != null && !botToken.isBlank();
    }

    /** Уведомление менеджеру (без HTML — текст от клиента может содержать спецсимволы). */
    public boolean sendMessage(String text) {
        if (chatId == null || chatId.isBlank()) {
            log.warn("Telegram chat-id is not configured");
            return false;
        }
        return sendPlain(chatId.trim(), text, mainMenuKeyboard());
    }

    public boolean sendMessageTo(String targetChatId, String text) {
        return sendPlain(targetChatId, text, mainMenuKeyboard());
    }

    public boolean sendMessageHtml(String targetChatId, String text, Map<String, Object> replyMarkup) {
        if (sendInternal(targetChatId, text, replyMarkup, true)) {
            return true;
        }
        return sendInternal(targetChatId, stripHtml(text), replyMarkup, false);
    }

    public boolean sendPlain(String targetChatId, String text, Map<String, Object> replyMarkup) {
        return sendInternal(targetChatId, text, replyMarkup, false);
    }

    private boolean sendInternal(String targetChatId, String text, Map<String, Object> replyMarkup, boolean html) {
        if (!isConfigured()) {
            log.warn("Telegram bot token not configured");
            return false;
        }
        if (targetChatId == null || targetChatId.isBlank()) {
            return false;
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", targetChatId.trim());
            body.put("text", text);
            body.put("disable_web_page_preview", true);
            if (html) {
                body.put("parse_mode", "HTML");
            }
            if (replyMarkup != null && !replyMarkup.isEmpty()) {
                body.put("reply_markup", replyMarkup);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    apiUrl("sendMessage"), new HttpEntity<>(body, jsonHeaders()), Map.class);

            return isOk(response);
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, Object> mainMenuKeyboard() {
        Map<String, Object> keyboard = new HashMap<>();
        keyboard.put("keyboard", List.of(
                List.of(
                        button(BTN_LEADS),
                        button(BTN_ORDERS)
                )
        ));
        keyboard.put("resize_keyboard", true);
        keyboard.put("persistent", true);
        return keyboard;
    }

    public Map<String, Object> inlineNavKeyboard(String prefix, int index, long total) {
        List<Map<String, String>> navRow = new ArrayList<>();
        if (index < total - 1) {
            navRow.add(button("◀️ Ранее", prefix + ":" + (index + 1)));
        }
        if (index > 0) {
            navRow.add(button("▶️ Новее", prefix + ":" + (index - 1)));
        }

        List<List<Map<String, String>>> rows = new ArrayList<>();
        if (!navRow.isEmpty()) {
            rows.add(navRow);
        }
        rows.add(List.of(button("🔙 В меню", "menu")));

        Map<String, Object> markup = new HashMap<>();
        markup.put("inline_keyboard", rows);
        return markup;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUpdates(long offset, int timeoutSeconds) {
        if (!isConfigured()) {
            return Map.of("ok", false);
        }
        String url = apiUrl("getUpdates") + "?timeout=" + timeoutSeconds + "&offset=" + offset;
        return restTemplate.getForObject(url, Map.class);
    }

    public void setWebhook(String webhookUrl, String secretToken) {
        if (!isConfigured()) {
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("url", webhookUrl);
        if (secretToken != null && !secretToken.isBlank()) {
            body.put("secret_token", secretToken.trim());
        }
        restTemplate.postForObject(apiUrl("setWebhook"), new HttpEntity<>(body, jsonHeaders()), Map.class);
        log.info("Telegram webhook set to {}", webhookUrl);
    }

    /** @return true если webhook снят или Telegram недоступен (не бросает исключение) */
    public boolean deleteWebhook() {
        if (!isConfigured()) {
            return false;
        }
        try {
            restTemplate.getForObject(apiUrl("deleteWebhook"), Map.class);
            return true;
        } catch (Exception e) {
            log.warn("Telegram deleteWebhook failed (network or API): {} — polling/webhook may be unavailable",
                    e.getMessage());
            return false;
        }
    }

    public boolean editMessageHtml(String targetChatId, long messageId, String text, Map<String, Object> replyMarkup) {
        if (!isConfigured() || targetChatId == null || targetChatId.isBlank()) {
            return false;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", targetChatId.trim());
            body.put("message_id", messageId);
            body.put("text", text);
            body.put("parse_mode", "HTML");
            body.put("disable_web_page_preview", true);
            if (replyMarkup != null && !replyMarkup.isEmpty()) {
                body.put("reply_markup", replyMarkup);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    apiUrl("editMessageText"), new HttpEntity<>(body, jsonHeaders()), Map.class);

            if (isOk(response)) {
                return true;
            }
            if (isNotModified(response)) {
                return true;
            }
            log.warn("Telegram editMessageText failed: {}", response);
            return false;
        } catch (Exception e) {
            log.warn("Failed to edit Telegram message: {}", e.getMessage());
            return false;
        }
    }

    public void answerCallbackQuery(String callbackQueryId) {
        if (!isConfigured() || callbackQueryId == null || callbackQueryId.isBlank()) {
            return;
        }
        try {
            Map<String, Object> body = Map.of("callback_query_id", callbackQueryId);
            restTemplate.postForObject(
                    apiUrl("answerCallbackQuery"), new HttpEntity<>(body, jsonHeaders()), Map.class);
        } catch (Exception e) {
            log.warn("Failed to answer callback query: {}", e.getMessage());
        }
    }

    private static Map<String, String> button(String text) {
        return Map.of("text", text);
    }

    private static Map<String, String> button(String text, String callbackData) {
        return Map.of("text", text, "callback_data", callbackData);
    }

    @SuppressWarnings("unchecked")
    private static boolean isOk(Map<String, Object> response) {
        return response != null && Boolean.TRUE.equals(response.get("ok"));
    }

    @SuppressWarnings("unchecked")
    private static boolean isNotModified(Map<String, Object> response) {
        if (response == null) {
            return false;
        }
        Object desc = response.get("description");
        return desc instanceof String s && s.contains("message is not modified");
    }

    private static String stripHtml(String html) {
        return html
                .replace("<b>", "").replace("</b>", "")
                .replace("<i>", "").replace("</i>", "")
                .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
    }

    private String apiUrl(String method) {
        return "https://api.telegram.org/bot" + botToken.trim() + "/" + method;
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
