package com.ihome24.ihome24.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    /** Строка состава заказа для письма */
    public static class OrderItemLine {
        private final String productName;
        private final int quantity;
        private final BigDecimal price;

        public OrderItemLine(String productName, int quantity, BigDecimal price) {
            this.productName = productName != null ? productName : "";
            this.quantity = quantity;
            this.price = price != null ? price : BigDecimal.ZERO;
        }

        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
    }

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.email.from:}")
    private String appEmailFrom;

    /** При таймауте/ошибке соединения с SMTP подсказать переключиться на 587 (часто решает проблему на хостинге) */
    private void logSmtpPortHint(Throwable e) {
        String msg = e != null ? e.getMessage() : "";
        if (msg == null) return;
        boolean looksLikeConnectionFailure = msg.contains("465") || msg.contains("Connect timed out")
                || msg.contains("MailConnectException") || msg.contains("connection failed");
        if (looksLikeConnectionFailure) {
            log.warn("SMTP недоступен с этого сервера. В .env.prod попробуйте порт 587: MAIL_PORT=587, MAIL_SMTP_SSL_ENABLE=false, MAIL_STARTTLS=true");
        }
    }

    public void sendVerificationCode(String toEmail, String code, String deviceInfo) {
        // Проверяем наличие конфигурации email
        if (mailUsername == null || mailUsername.isEmpty()) {
            String errorMsg = "Email конфигурация не настроена. Установите переменные окружения MAIL_USERNAME и MAIL_PASSWORD. " +
                    "См. backend/TIMEWEB_SMTP_SETUP.md для инструкций.";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        // Логируем используемые настройки (без пароля)
        log.info("Attempting to send email using SMTP:");
        log.info("  Host: smtp.timeweb.ru");
        log.info("  Username: {}", mailUsername);
        log.info("  Sending verification code to: {}", toEmail);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            // Для Timeweb важно использовать тот же адрес, что и username
            String fromEmail = mailUsername != null && !mailUsername.isEmpty() ? mailUsername : toEmail;
            
            // Проверяем, что код действительно 6-значный
            if (code == null || code.length() != 6) {
                log.error("Invalid verification code format: expected 6 digits, got length: {}", 
                        code != null ? code.length() : 0);
                throw new RuntimeException("Invalid verification code format: expected 6 digits, got: " + (code != null ? code.length() : 0) + " digits");
            }
            
            // Маскируем код в логах (показываем только первые 2 символа)
            String maskedCode = code.substring(0, 2) + "****";
            log.info("Preparing email: from={}, to={}, code={}", fromEmail, toEmail, maskedCode);
            
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Код подтверждения авторизации");
            
            // Создаем улучшенное содержимое письма для избежания спама
            String plainTextBody = String.format(
                "Здравствуйте!\n\n" +
                "Вы пытаетесь войти в систему iHome24 с нового устройства.\n\n" +
                "Ваш код подтверждения: %s\n\n" +
                "Код действителен в течение 15 минут.\n\n" +
                "Если вы не пытались войти в систему, пожалуйста, проигнорируйте это письмо или свяжитесь с нашей службой поддержки.\n\n" +
                "С уважением,\n" +
                "Команда iHome24\n" +
                "https://ihome24.ru",
                code
            );
            
            // HTML версия письма для лучшей доставляемости
            String htmlBody = String.format(
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "  <div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px;\">\n" +
                "    <h2 style=\"color: #2c3e50; margin-top: 0;\">Код подтверждения входа</h2>\n" +
                "    <p>Здравствуйте!</p>\n" +
                "    <p>Вы пытаетесь войти в систему <strong>iHome24</strong> с нового устройства.</p>\n" +
                "    <div style=\"background-color: #ffffff; border: 2px solid #3498db; border-radius: 5px; padding: 20px; text-align: center; margin: 20px 0;\">\n" +
                "      <p style=\"margin: 0; font-size: 14px; color: #7f8c8d;\">Ваш код подтверждения:</p>\n" +
                "      <p style=\"margin: 10px 0; font-size: 32px; font-weight: bold; color: #2c3e50; letter-spacing: 5px;\">%s</p>\n" +
                "    </div>\n" +
                "    <p style=\"color: #7f8c8d; font-size: 14px;\">Код действителен в течение <strong>15 минут</strong>.</p>\n" +
                "    <hr style=\"border: none; border-top: 1px solid #ecf0f1; margin: 20px 0;\">\n" +
                "    <p style=\"color: #95a5a6; font-size: 12px;\">Если вы не пытались войти в систему, пожалуйста, проигнорируйте это письмо или свяжитесь с нашей службой поддержки.</p>\n" +
                "  </div>\n" +
                "  <p style=\"color: #7f8c8d; font-size: 12px; text-align: center;\">\n" +
                "    С уважением,<br>\n" +
                "    Команда iHome24<br>\n" +
                "    <a href=\"https://ihome24.ru\" style=\"color: #3498db;\">ihome24.ru</a>\n" +
                "  </p>\n" +
                "</body>\n" +
                "</html>",
                code
            );
            
            message.setText(plainTextBody);
            
            log.info("Attempting to send email via SMTP...");
            
            // Используем MimeMessage с HTML версией для лучшей доставляемости
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail, "iHome24"); // Добавляем имя отправителя
                helper.setTo(toEmail);
                helper.setSubject("Код подтверждения входа в iHome24");
                helper.setText(plainTextBody, htmlBody); // plain text и HTML версии
                
                // Добавляем правильные заголовки для избежания спама
                mimeMessage.setHeader("X-Mailer", "iHome24 Mail System");
                mimeMessage.setHeader("X-Priority", "3"); // Нормальный приоритет (не высокий, чтобы не выглядеть как спам)
                mimeMessage.setHeader("Precedence", "bulk");
                mimeMessage.setHeader("Auto-Submitted", "auto-generated");
                mimeMessage.setHeader("List-Unsubscribe", "<mailto:" + fromEmail + ">");
                mimeMessage.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
                
                log.info("Sending MimeMessage via SMTP...");
                mailSender.send(mimeMessage);
                log.info("✓ Email successfully sent via SMTP to: {}", toEmail);
            } catch (Exception mimeException) {
                log.warn("Failed to send MimeMessage, trying SimpleMailMessage: {}", mimeException.getMessage());
                // Fallback to SimpleMailMessage
                mailSender.send(message);
                log.info("✓ SimpleMailMessage successfully sent via SMTP to: {}", toEmail);
                log.info("  Note: Email may go to spam folder. User should mark it as 'Not Spam' in Gmail.");
            }
        } catch (org.springframework.mail.MailAuthenticationException e) {
            String errorMsg = "Ошибка аутентификации SMTP (Timeweb). Проверьте правильность MAIL_USERNAME и MAIL_PASSWORD. " +
                    "См. backend/TIMEWEB_SMTP_SETUP.md для инструкций.";
            log.error("✗ SMTP Authentication failed!");
            log.error("  Error: {}", e.getMessage());
            log.error("  Username: {}", mailUsername);
            log.error("  Host: smtp.timeweb.ru");
            log.error("  Port: 587 (TLS)");
            log.error("  Check: MAIL_USERNAME and MAIL_PASSWORD environment variables");
            throw new RuntimeException(errorMsg, e);
        } catch (org.springframework.mail.MailSendException e) {
            log.error("✗ Failed to send email via SMTP!");
            log.error("  To: {}", toEmail);
            log.error("  From: {}", mailUsername);
            log.error("  Error: {}", e.getMessage());
            if (e.getFailedMessages() != null && !e.getFailedMessages().isEmpty()) {
                log.error("  Failed messages details: {}", e.getFailedMessages());
            }
            String errorMsg = "Не удалось отправить email через SMTP. Проверьте конфигурацию и сетевые настройки. " +
                    "Ошибка: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            log.error("✗ Unexpected error while sending email!");
            log.error("  To: {}", toEmail);
            log.error("  Error type: {}", e.getClass().getSimpleName());
            log.error("  Error message: {}", e.getMessage());
            log.error("  Stack trace:", e);
            String errorMsg = "Не удалось отправить email. Неожиданная ошибка: " +
                    (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Отправка письма подтверждения заказа. Не бросает исключение при ненастроенной почте —
     * заказ всё равно создаётся, письмо просто не отправляется.
     */
    public void sendOrderConfirmation(String toEmail, String customerName, Long orderNumber, String totalAmount,
                                      List<OrderItemLine> orderLines) {
        if (mailUsername == null || mailUsername.isEmpty()) {
            log.warn("Email не настроен — письмо подтверждения заказа #{} не отправлено. Укажите MAIL_USERNAME и MAIL_PASSWORD в .env.prod", orderNumber);
            return;
        }
        try {
            String fromEmail = mailUsername;
            String name = customerName != null && !customerName.isBlank() ? customerName : "клиент";
            String total = totalAmount != null && !totalAmount.isBlank() ? totalAmount : "—";

            String linesStr = orderLines != null && !orderLines.isEmpty()
                    ? orderLines.stream()
                            .map(line -> String.format("%s × %d — %s ₽", line.getProductName(), line.getQuantity(), line.getPrice()))
                            .collect(Collectors.joining("\n"))
                    : "—";
            String plainText = String.format(
                    "Здравствуйте, %s!\n\nВаш заказ №%s принят.\nИтого к оплате: %s\n\nСостав заказа:\n%s\n\nС уважением,\niHome24 — ihome24.ru",
                    name, orderNumber, total, linesStr);

            StringBuilder rows = new StringBuilder();
            if (orderLines != null && !orderLines.isEmpty()) {
                for (OrderItemLine line : orderLines) {
                    String productName = line.getProductName() != null ? escapeHtml(line.getProductName()) : "—";
                    String price = line.getPrice() != null ? line.getPrice().toString() : "0";
                    rows.append(String.format(
                            "<tr><td style=\"padding:10px 12px;border-bottom:1px solid #eee;\">%s</td><td style=\"padding:10px 12px;border-bottom:1px solid #eee;text-align:center;\">%d</td><td style=\"padding:10px 12px;border-bottom:1px solid #eee;text-align:right;\">%s ₽</td></tr>",
                            productName, line.getQuantity(), price));
                }
            }

            String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head><body style=\"margin:0;font-family:Arial,sans-serif;font-size:16px;line-height:1.5;color:#333;background:#f5f5f5;\">"
                    + "<div style=\"max-width:560px;margin:24px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
                    + "<div style=\"background:#2563eb;color:#fff;padding:20px 24px;\"><h1 style=\"margin:0;font-size:22px;font-weight:600;\">Подтверждение заказа №" + orderNumber + "</h1><p style=\"margin:8px 0 0;opacity:0.9;font-size:14px;\">iHome24</p></div>"
                    + "<div style=\"padding:24px;\">"
                    + "<p style=\"margin:0 0 16px;\">Здравствуйте, <strong>" + escapeHtml(name) + "</strong>!</p>"
                    + "<p style=\"margin:0 0 20px;\">Ваш заказ принят. Ниже состав и сумма.</p>"
                    + "<table style=\"width:100%;border-collapse:collapse;background:#fafafa;border-radius:8px;\"><thead><tr style=\"background:#f1f5f9;\"><th style=\"padding:10px 12px;text-align:left;font-size:13px;color:#64748b;\">Товар</th><th style=\"padding:10px 12px;text-align:center;font-size:13px;color:#64748b;\">Кол-во</th><th style=\"padding:10px 12px;text-align:right;font-size:13px;color:#64748b;\">Цена</th></tr></thead><tbody>" + rows + "</tbody></table>"
                    + "<p style=\"margin:20px 0 0;padding:16px;background:#eff6ff;border-radius:8px;font-size:18px;font-weight:600;\">Итого к оплате: " + escapeHtml(total) + " ₽</p>"
                    + "</div>"
                    + "<div style=\"padding:16px 24px;background:#f8fafc;border-top:1px solid #e2e8f0;font-size:14px;color:#64748b;\">С уважением,<br><strong>iHome24</strong> — <a href=\"https://ihome24.ru\" style=\"color:#2563eb;\">ihome24.ru</a></div>"
                    + "</div></body></html>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail, "iHome24");
            helper.setTo(toEmail);
            helper.setSubject("Подтверждение заказа №" + orderNumber + " — iHome24");
            helper.setText(plainText, html);
            mailSender.send(mimeMessage);
            log.info("Письмо подтверждения заказа #{} отправлено на {}", orderNumber, toEmail);
        } catch (Exception e) {
            log.error("Не удалось отправить письмо подтверждения заказа #{}: {}", orderNumber, e.getMessage());
            logSmtpPortHint(e);
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * Уведомление об изменении статуса заказа. Не бросает исключение при ненастроенной почте.
     */
    public void sendOrderStatusChange(String toEmail, String customerName, Long orderNumber, String newStatusDisplay) {
        if (mailUsername == null || mailUsername.isEmpty()) {
            log.warn("Email не настроен — уведомление о статусе заказа #{} не отправлено.", orderNumber);
            return;
        }
        try {
            String name = customerName != null && !customerName.isBlank() ? customerName : "клиент";
            String status = newStatusDisplay != null && !newStatusDisplay.isBlank() ? newStatusDisplay : "обновлён";

            String plainText = String.format(
                    "Здравствуйте, %s!\n\nСтатус вашего заказа №%s: %s\n\nС уважением,\niHome24 — ihome24.ru",
                    name, orderNumber, status);

            String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head><body style=\"margin:0;font-family:Arial,sans-serif;font-size:16px;line-height:1.5;color:#333;background:#f5f5f5;\">"
                    + "<div style=\"max-width:560px;margin:24px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
                    + "<div style=\"background:#0d9488;color:#fff;padding:20px 24px;\"><h1 style=\"margin:0;font-size:22px;font-weight:600;\">Статус заказа №" + orderNumber + "</h1><p style=\"margin:8px 0 0;opacity:0.9;font-size:14px;\">iHome24</p></div>"
                    + "<div style=\"padding:24px;\">"
                    + "<p style=\"margin:0 0 16px;\">Здравствуйте, <strong>" + escapeHtml(name) + "</strong>!</p>"
                    + "<p style=\"margin:0 0 20px;\">Текущий статус вашего заказа:</p>"
                    + "<p style=\"margin:0;padding:16px;background:#f0fdfa;border-radius:8px;font-size:18px;font-weight:600;color:#0d9488;\">" + escapeHtml(status) + "</p>"
                    + "</div>"
                    + "<div style=\"padding:16px 24px;background:#f8fafc;border-top:1px solid #e2e8f0;font-size:14px;color:#64748b;\">С уважением,<br><strong>iHome24</strong> — <a href=\"https://ihome24.ru\" style=\"color:#2563eb;\">ihome24.ru</a></div>"
                    + "</div></body></html>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailUsername, "iHome24");
            helper.setTo(toEmail);
            helper.setSubject("Статус заказа №" + orderNumber + " — iHome24");
            helper.setText(plainText, html);
            mailSender.send(mimeMessage);
            log.info("Уведомление о статусе заказа #{} отправлено на {}", orderNumber, toEmail);
        } catch (Exception e) {
            log.error("Не удалось отправить уведомление о статусе заказа #{}: {}", orderNumber, e.getMessage());
            logSmtpPortHint(e);
        }
    }
}
