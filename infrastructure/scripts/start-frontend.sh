#!/bin/bash

# Скрипт для запуска Frontend

set -e

# Загружаем переменные окружения
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "🌐 Запуск Frontend..."

# Проверяем наличие backend
if ! docker-compose ps backend 2>/dev/null | grep -q "Up"; then
    echo "⚠️  Backend не запущен. Запускаю..."
    ./scripts/start-backend.sh
fi

docker-compose up -d --build frontend

echo "⏳ Ожидание готовности Frontend..."
sleep 5

docker-compose ps frontend

echo "✅ Frontend запущен!"
echo "   URL: http://localhost:${FRONTEND_PORT:-3000}"
