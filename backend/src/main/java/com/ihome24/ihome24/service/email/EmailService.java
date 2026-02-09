package com.ihome24.ihome24.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.email.from:}")
    private String appEmailFrom;

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
     * Отправляет клиенту письмо о принятии заказа.
     * Текст: заказ принят, обрабатывается, скоро с вами свяжется менеджер.
     */
    public void sendOrderConfirmation(String toEmail, String customerName, Long orderNumber, String totalAmount) {
        if (mailUsername == null || mailUsername.isEmpty()) {
            log.warn("Email не настроен. Письмо о заказе #{} не отправлено.", orderNumber);
            return;
        }
        try {
            String fromEmail = mailUsername;
            String subject = "Ваш заказ #" + orderNumber + " принят — iHome24";

            String plainText = String.format(
                "Здравствуйте, %s!\n\n" +
                "Ваш заказ #%d успешно принят и начинает обрабатываться.\n\n" +
                "Сумма заказа: %s\n\n" +
                "Скоро с вами свяжется наш менеджер для уточнения деталей доставки.\n" +
                "Пожалуйста, будьте на связи по указанному телефону.\n\n" +
                "С уважением,\n" +
                "Команда iHome24\n" +
                "https://ihome24.ru",
                customerName != null ? customerName : "Клиент",
                orderNumber,
                totalAmount != null ? totalAmount : ""
            );

            String htmlBody = String.format(
                "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "<div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px;\">" +
                "<h2 style=\"color: #2c3e50;\">Заказ #%d принят</h2>" +
                "<p>Здравствуйте, %s!</p>" +
                "<p>Ваш заказ успешно принят и <strong>начинает обрабатываться</strong>.</p>" +
                "<p><strong>Сумма заказа:</strong> %s</p>" +
                "<p>Скоро с вами свяжется наш менеджер для уточнения деталей доставки. Пожалуйста, будьте на связи.</p>" +
                "</div>" +
                "<p style=\"color: #7f8c8d; font-size: 12px;\">С уважением,<br>Команда iHome24<br><a href=\"https://ihome24.ru\">ihome24.ru</a></p>" +
                "</body></html>",
                orderNumber,
                customerName != null ? customerName : "Клиент",
                totalAmount != null ? totalAmount : ""
            );

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail, "iHome24");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainText, htmlBody);

            mailSender.send(mimeMessage);
            log.info("Письмо о заказе #{} отправлено на {}", orderNumber, toEmail);
        } catch (Exception e) {
            log.error("Ошибка отправки письма о заказе #{}: {}", orderNumber, e.getMessage());
            // Не бросаем исключение - заказ уже создан, письмо не критично
        }
    }
}
