package com.ihome24.ihome24.service.telegram;

import com.ihome24.ihome24.entity.order.Order;
import com.ihome24.ihome24.entity.wholesale.WholesaleLead;
import com.ihome24.ihome24.entity.wholesale.WholesaleLeadStatus;
import com.ihome24.ihome24.repository.order.OrderRepository;
import com.ihome24.ihome24.repository.wholesale.WholesaleLeadRepository;
import com.ihome24.ihome24.service.wholesale.WholesaleLeadAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotHandler {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Duration NEW_THRESHOLD = Duration.ofHours(24);
    private static final String SEP = "━━━━━━━━━━━━━━━━";

    private final TelegramBotService telegramBotService;
    private final WholesaleLeadRepository wholesaleLeadRepository;
    private final OrderRepository orderRepository;

    @Value("${app.telegram.chat-id:}")
    private String defaultChatId;

    @Value("${app.telegram.allowed-chat-ids:}")
    private String allowedChatIds;

    @SuppressWarnings("unchecked")
    public void handleUpdate(Map<String, Object> update) {
        if (update == null) {
            return;
        }

        try {
            if (update.containsKey("callback_query")) {
                handleCallbackQuery((Map<String, Object>) update.get("callback_query"));
                return;
            }

            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message == null) {
                return;
            }

            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            if (chat == null || chat.get("id") == null) {
                return;
            }

            String chatId = String.valueOf(chat.get("id"));
            if (!isAllowedChat(chatId)) {
                log.warn("Telegram message from unauthorized chat {}", chatId);
                return;
            }

            String text = message.get("text") instanceof String s ? s.trim() : "";
            if (text.isEmpty()) {
                return;
            }

            log.info("Telegram command from {}: {}", chatId, text);

            if (text.startsWith("/start") || text.equalsIgnoreCase("/menu") || text.equalsIgnoreCase("меню")) {
                sendWelcome(chatId);
            } else if (text.equals(TelegramBotService.BTN_LEADS) || text.contains("Заявки")) {
                sendLeadCard(chatId, null, 0);
            } else if (text.equals(TelegramBotService.BTN_ORDERS) || text.contains("Заказы")) {
                sendOrderCard(chatId, null, 0);
            } else {
                telegramBotService.sendMessageTo(chatId, "Выберите раздел кнопками ниже 👇");
            }
        } catch (Exception e) {
            log.error("Telegram handler error: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCallbackQuery(Map<String, Object> callback) {
        if (callback == null) {
            return;
        }

        Object queryId = callback.get("id");
        if (queryId != null) {
            telegramBotService.answerCallbackQuery(String.valueOf(queryId));
        }

        Map<String, Object> message = (Map<String, Object>) callback.get("message");
        if (message == null) {
            return;
        }

        Map<String, Object> chat = (Map<String, Object>) message.get("chat");
        if (chat == null || chat.get("id") == null) {
            return;
        }

        String chatId = String.valueOf(chat.get("id"));
        if (!isAllowedChat(chatId)) {
            return;
        }

        Number messageIdNum = (Number) message.get("message_id");
        Long messageId = messageIdNum != null ? messageIdNum.longValue() : null;

        String data = callback.get("data") instanceof String s ? s.trim() : "";
        if (data.equals("menu")) {
            Map<String, Object> clearInline = Map.of("inline_keyboard", List.of());
            if (messageId != null) {
                telegramBotService.editMessageHtml(chatId, messageId, welcomeText(), clearInline);
            } else {
                sendWelcome(chatId);
            }
            return;
        }

        if (data.startsWith("lead:")) {
            int index = parseIndex(data.substring(5));
            sendLeadCard(chatId, messageId, index);
        } else if (data.startsWith("order:")) {
            int index = parseIndex(data.substring(6));
            sendOrderCard(chatId, messageId, index);
        }
    }

    public void sendWelcome(String chatId) {
        telegramBotService.sendPlain(chatId, welcomeText(), telegramBotService.mainMenuKeyboard());
    }

    private static String welcomeText() {
        return "iHome24 — панель менеджера\n\n"
                + TelegramBotService.BTN_LEADS + " — просмотр заявок\n"
                + TelegramBotService.BTN_ORDERS + " — просмотр заказов\n\n"
                + "Листайте: ◀️ Ранее (старые) · ▶️ Новее (свежие)";
    }

    private void sendLeadCard(String chatId, Long messageId, int index) {
        long total = wholesaleLeadRepository.count();
        if (total == 0) {
            sendOrEdit(chatId, messageId, "📋 <b>Заявок пока нет</b>\n\nОни появятся после отправки формы на сайте.", null);
            return;
        }

        int safeIndex = Math.max(0, Math.min(index, (int) total - 1));
        List<WholesaleLead> page = wholesaleLeadRepository.findWithFilters(
                null, null, PageRequest.of(safeIndex, 1, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
        if (page.isEmpty()) {
            sendOrEdit(chatId, messageId, "📋 Заявка не найдена.", telegramBotService.inlineNavKeyboard("lead", safeIndex, total));
            return;
        }

        WholesaleLead lead = page.get(0);
        String text = formatLeadCard(lead, safeIndex, total);
        Map<String, Object> keyboard = telegramBotService.inlineNavKeyboard("lead", safeIndex, total);
        sendOrEdit(chatId, messageId, text, keyboard);
    }

    private void sendOrderCard(String chatId, Long messageId, int index) {
        long total = orderRepository.countRecentOrders();
        if (total == 0) {
            sendOrEdit(chatId, messageId, "📦 <b>Заказов пока нет</b>\n\nОни появятся после оформления на сайте.", null);
            return;
        }

        int safeIndex = Math.max(0, Math.min(index, (int) total - 1));
        List<Order> page = orderRepository.findRecentOrders(PageRequest.of(safeIndex, 1));
        if (page.isEmpty()) {
            sendOrEdit(chatId, messageId, "📦 Заказ не найден.", telegramBotService.inlineNavKeyboard("order", safeIndex, total));
            return;
        }

        Order order = page.get(0);
        String text = formatOrderCard(order, safeIndex, total);
        Map<String, Object> keyboard = telegramBotService.inlineNavKeyboard("order", safeIndex, total);
        sendOrEdit(chatId, messageId, text, keyboard);
    }

    private void sendOrEdit(String chatId, Long messageId, String text, Map<String, Object> inlineKeyboard) {
        if (messageId != null && telegramBotService.editMessageHtml(chatId, messageId, text, inlineKeyboard)) {
            return;
        }
        telegramBotService.sendMessageHtml(chatId, text, inlineKeyboard);
    }

    private String formatLeadCard(WholesaleLead lead, int index, long total) {
        LocalDateTime created = lead.getCreatedAt();
        String badge = badgeForLead(lead, index);

        StringBuilder sb = new StringBuilder();
        sb.append("📋 <b>Заявка #").append(lead.getId()).append("</b> · ").append(index + 1).append(" из ").append(total).append('\n');
        sb.append(badge).append('\n');
        sb.append(SEP).append("\n\n");
        if (created != null) {
            sb.append("🕐 ").append(esc(created.format(DT)));
            sb.append(" · ").append(relativeTime(created)).append("\n\n");
        }
        sb.append("👤 <b>Клиент:</b> ").append(esc(lead.getName())).append('\n');
        sb.append("📞 <b>Телефон:</b> ").append(esc(lead.getPhone())).append('\n');
        if (lead.getInn() != null && !lead.getInn().isBlank()) {
            sb.append("🏢 <b>ИНН:</b> ").append(esc(lead.getInn())).append('\n');
        }
        if (lead.getMessage() != null && !lead.getMessage().isBlank()) {
            sb.append("\n💬 <b>Комментарий:</b>\n").append(esc(lead.getMessage())).append('\n');
        }
        sb.append('\n').append(SEP);
        sb.append("\n<i>◀️ Ранее — старые · ▶️ Новее — свежие</i>");
        return sb.toString();
    }

    private static String badgeForLead(WholesaleLead lead, int index) {
        if (lead.getStatus() == WholesaleLeadStatus.IN_PROGRESS && lead.getAssignedManager() != null) {
            return "👷 <b>В работе</b> · " + esc(WholesaleLeadAdminService.displayName(lead.getAssignedManager()));
        }
        if (lead.getStatus() == WholesaleLeadStatus.DONE) {
            String manager = lead.getAssignedManager() != null
                    ? " · " + esc(WholesaleLeadAdminService.displayName(lead.getAssignedManager()))
                    : "";
            return "✅ <b>Завершена</b>" + manager;
        }
        return badgeFor(lead.getCreatedAt(), index);
    }

    private String formatOrderCard(Order order, int index, long total) {
        LocalDateTime date = order.getOrderDate() != null ? order.getOrderDate() : order.getCreatedAt();
        String badge = badgeFor(date, index);

        StringBuilder sb = new StringBuilder();
        sb.append("📦 <b>Заказ №").append(order.getOrderNumber()).append("</b>\n");
        sb.append("<b>").append(index + 1).append(" из ").append(total).append("</b>\n");
        sb.append(badge).append('\n');
        sb.append(SEP).append("\n\n");
        if (date != null) {
            sb.append("🕐 ").append(esc(date.format(DT)));
            sb.append(" · ").append(relativeTime(date)).append('\n');
        }
        sb.append("📊 <b>Статус:</b> ").append(esc(statusRu(order.getStatus()))).append('\n');
        sb.append("💰 <b>Сумма:</b> ").append(esc(formatMoney(order.getSpent()))).append("\n\n");
        sb.append("👤 <b>Клиент:</b> ").append(esc(safe(order.getCustomer()))).append('\n');
        if (order.getPhone() != null && !order.getPhone().isBlank()) {
            sb.append("📞 <b>Телефон:</b> ").append(esc(order.getPhone())).append('\n');
        }
        if (order.getEmail() != null && !order.getEmail().isBlank()) {
            sb.append("✉️ <b>Email:</b> ").append(esc(order.getEmail())).append('\n');
        }
        if (order.getAddress() != null && !order.getAddress().isBlank()) {
            sb.append("\n🏠 <b>Адрес:</b>\n").append(esc(order.getAddress())).append('\n');
        }
        if (order.getComment() != null && !order.getComment().isBlank()) {
            sb.append("\n💬 <b>Комментарий:</b>\n").append(esc(order.getComment())).append('\n');
        }
        sb.append('\n').append(SEP);
        sb.append("\n<i>◀️ Ранее — старые · ▶️ Новее — свежие</i>");
        return sb.toString();
    }

    private static String badgeFor(LocalDateTime dateTime, int index) {
        if (index == 0) {
            if (dateTime != null && Duration.between(dateTime, LocalDateTime.now()).compareTo(NEW_THRESHOLD) <= 0) {
                return "🆕 <b>НОВАЯ</b>";
            }
            return "🆕 <b>Последняя</b>";
        }
        return "📂 <b>Ранее</b>";
    }

    private static String relativeTime(LocalDateTime dateTime) {
        Duration d = Duration.between(dateTime, LocalDateTime.now());
        if (d.isNegative()) {
            return "только что";
        }
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "только что";
        }
        if (minutes < 60) {
            return minutes + " мин. назад";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + " ч. назад";
        }
        long days = d.toDays();
        if (days == 1) {
            return "вчера";
        }
        return days + " дн. назад";
    }

    private static int parseIndex(String raw) {
        try {
            return Math.max(0, Integer.parseInt(raw.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isAllowedChat(String chatId) {
        Set<String> allowed = Arrays.stream(allowedChatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (allowed.isEmpty() && defaultChatId != null && !defaultChatId.isBlank()) {
            allowed.add(defaultChatId.trim());
        }

        return allowed.contains(chatId);
    }

    private static String statusRu(Order.OrderStatus status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case PRELIMINARY -> "Предварительный";
            case PENDING -> "Ожидает";
            case IN_PROCESSING -> "В обработке";
            case DISPATCHED -> "Отправлено";
            case OUT_FOR_DELIVERY -> "В доставке";
            case READY_TO_PICKUP -> "Готово к выдаче";
            case DELIVERED -> "Доставлено";
        };
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "—";
        }
        return String.format("%.0f ₽", amount);
    }

    private static String safe(String value) {
        return value != null && !value.isBlank() ? value : "—";
    }

    private static String esc(String value) {
        if (value == null) {
            return "—";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
