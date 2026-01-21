#!/bin/bash

# Скрипт для запуска Backend

set -e

# Загружаем переменные окружения
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "🔧 Запуск Backend..."

# Проверяем наличие зависимостей
if ! docker-compose ps postgres 2>/dev/null | grep -q "Up"; then
    echo "⚠️  PostgreSQL не запущен. Запускаю..."
    docker-compose up -d postgres
    sleep 5
fi

if ! docker-compose ps minio 2>/dev/null | grep -q "Up"; then
    echo "⚠️  MinIO не запущен. Запускаю..."
    docker-compose up -d minio minio-setup
    sleep 5
fi

# Создаем директорию для логов
mkdir -p logs/backend

docker-compose up -d --build backend

echo "⏳ Ожидание готовности Backend..."
sleep 10

docker-compose ps backend

echo "✅ Backend запущен!"
echo "   API: http://localhost:${BACKEND_PORT:-8080}"
