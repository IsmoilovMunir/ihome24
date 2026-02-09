#!/bin/bash

# Скрипт для запуска приложения с настройками email
# Использование: ./run-with-email.sh
#
# Настройка: создайте backend/.env с MAIL_USERNAME и MAIL_PASSWORD
# Или экспортируйте: export MAIL_USERNAME=info@ihome24.ru MAIL_PASSWORD=...

echo "🚀 Запуск приложения с настройками Timeweb SMTP..."
echo ""

# Загружаем .env если есть (пароль только из env, не из репозитория!)
if [ -f .env ]; then
  set -a
  source .env
  set +a
  echo "✓ Загружен backend/.env"
fi

if [ -z "$MAIL_USERNAME" ] || [ -z "$MAIL_PASSWORD" ]; then
  echo "⚠ Нет MAIL_USERNAME или MAIL_PASSWORD. Email не будет работать."
  echo "  Создайте backend/.env по образцу backend/.env.example"
  echo ""
fi

echo "SMTP: ${MAIL_HOST:-smtp.timeweb.ru}:${MAIL_PORT:-587}"
echo ""

mvn spring-boot:run
