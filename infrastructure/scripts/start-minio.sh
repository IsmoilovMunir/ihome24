#!/bin/bash

# Скрипт для запуска MinIO

set -e

# Загружаем переменные окружения
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "📦 Запуск MinIO..."

docker-compose up -d minio minio-setup

echo "⏳ Ожидание готовности MinIO..."
sleep 5

docker-compose ps minio

echo "✅ MinIO запущен!"
echo "   API: http://localhost:${MINIO_PORT:-9000}"
echo "   Console: http://localhost:${MINIO_CONSOLE_PORT:-9001}"
echo "   Логин: ${MINIO_ROOT_USER:-iHome24mino}"
echo "   Пароль: ${MINIO_ROOT_PASSWORD:-iHome242025mino}"
