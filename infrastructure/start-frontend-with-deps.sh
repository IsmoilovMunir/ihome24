#!/bin/bash

# Скрипт для запуска Frontend с автоматическим запуском зависимостей

set -e

# Загружаем переменные окружения если есть .env файл
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "🌐 Запуск Frontend с зависимостями..."

# Запускаем Backend если он не запущен
if ! docker-compose ps backend | grep -q "Up"; then
    echo "📦 Запуск Backend..."
    ./start-backend-with-deps.sh
fi

# Запускаем Frontend
docker-compose up -d --build frontend

echo "⏳ Ожидание готовности Frontend..."
sleep 5

# Проверяем статус
if docker-compose ps frontend | grep -q "Up"; then
    echo ""
    echo "✅ Frontend запущен!"
    echo "   - Container: ihome24-frontend"
    echo "   - URL: http://localhost:${FRONTEND_PORT:-3000}"
    echo ""
    echo "📝 Полезные команды:"
    echo "   Логи: docker-compose logs -f frontend"
    echo "   Остановка: ./stop-frontend.sh"
else
    echo "❌ Ошибка запуска Frontend"
    docker-compose logs frontend
    exit 1
fi
