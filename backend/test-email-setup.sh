#!/bin/bash

# Скрипт для тестирования Timeweb SMTP
# Использование: source test-email-setup.sh

export MAIL_HOST=smtp.timeweb.ru
export MAIL_PORT=587
export MAIL_USERNAME=info@ihome24.ru
export MAIL_PASSWORD=cb3e101wu2
export APP_EMAIL_FROM=info@ihome24.ru
export MAIL_SMTP_SSL_ENABLE=false
export MAIL_SMTP_STARTTLS_ENABLE=true

echo "✅ Переменные окружения для Timeweb SMTP установлены:"
echo "   MAIL_HOST: $MAIL_HOST"
echo "   MAIL_PORT: $MAIL_PORT"
echo "   MAIL_USERNAME: $MAIL_USERNAME"
echo "   APP_EMAIL_FROM: $APP_EMAIL_FROM"
echo "   MAIL_PASSWORD: *** (скрыт)"
echo ""
echo "Теперь запустите приложение в ЭТОМ ЖЕ терминале:"
echo "   mvn spring-boot:run"
echo ""
echo "Или если используете IDE, добавьте эти переменные в конфигурацию запуска"
