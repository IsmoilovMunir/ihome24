#!/bin/bash

# Скрипт для запуска Admin Panel с автоматическим запуском зависимостей

set -e

# Загружаем переменные окружения если есть .env файл
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "👨‍💼 Запуск Admin Panel с зависимостями..."

# Запускаем Backend если он не запущен
if ! docker-compose ps backend | grep -q "Up"; then
    echo "📦 Запуск Backend..."
    ./start-backend-with-deps.sh
fi

# Запускаем Admin Panel
docker-compose up -d --build admin

echo "⏳ Ожидание готовности Admin Panel..."
sleep 5

# Проверяем статус
if docker-compose ps admin | grep -q "Up"; then
    echo ""
    echo "✅ Admin Panel запущен!"
    echo "   - Container: ihome24-admin"
    echo "   - URL: http://localhost:${ADMIN_PORT:-5173}"
    echo ""
    echo "📝 Полезные команды:"
    echo "   Логи: docker-compose logs -f admin"
    echo "   Остановка: ./stop-admin.sh"
else
    echo "❌ Ошибка запуска Admin Panel"
    docker-compose logs admin
    exit 1
fi
