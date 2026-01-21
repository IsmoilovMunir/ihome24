#!/bin/bash

# Скрипт для запуска Admin Panel

set -e

# Загружаем переменные окружения
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "👨‍💼 Запуск Admin Panel..."

# Проверяем наличие backend
if ! docker-compose ps backend 2>/dev/null | grep -q "Up"; then
    echo "⚠️  Backend не запущен. Запускаю..."
    ./scripts/start-backend.sh
fi

docker-compose up -d --build admin

echo "⏳ Ожидание готовности Admin Panel..."
sleep 5

docker-compose ps admin

echo "✅ Admin Panel запущен!"
echo "   URL: http://localhost:${ADMIN_PORT:-5173}"
