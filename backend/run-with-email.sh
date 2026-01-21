#!/bin/bash

# Скрипт для запуска приложения с настройками email
# Использование: ./run-with-email.sh

echo "🚀 Запуск приложения с настройками Timeweb SMTP..."
echo ""
echo "Email: info@ihome24.ru"
echo "SMTP: smtp.timeweb.ru:465"
echo ""

# Запускаем приложение (настройки уже в application.yml)
mvn spring-boot:run
