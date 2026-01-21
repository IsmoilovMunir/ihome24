#!/bin/bash

# Скрипт для запуска Backend с автоматическим запуском зависимостей

set -e

# Загружаем переменные окружения если есть .env файл
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "🔧 Запуск Backend с зависимостями..."

# Запускаем зависимости если они не запущены
if ! docker-compose ps postgres | grep -q "Up"; then
    echo "📦 Запуск PostgreSQL..."
    docker-compose up -d postgres
    sleep 5
fi

if ! docker-compose ps minio | grep -q "Up"; then
    echo "📦 Запуск MinIO..."
    docker-compose up -d minio minio-setup
    sleep 5
fi

# Запускаем Backend
docker-compose up -d --build backend

echo "⏳ Ожидание готовности Backend..."
sleep 10

# Проверяем статус
if docker-compose ps backend | grep -q "Up"; then
    echo ""
    echo "✅ Backend запущен!"
    echo "   - Container: ihome24-backend"
    echo "   - API: http://localhost:${BACKEND_PORT:-8080}"
    echo ""
    echo "📝 Полезные команды:"
    echo "   Логи: docker-compose logs -f backend"
    echo "   Остановка: ./stop-backend.sh"
else
    echo "❌ Ошибка запуска Backend"
    docker-compose logs backend
    exit 1
fi
