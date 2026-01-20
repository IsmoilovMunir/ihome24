#!/bin/bash

# Скрипт для запуска PostgreSQL

set -e

# Загружаем переменные окружения
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

echo "🐘 Запуск PostgreSQL..."

docker-compose up -d postgres

echo "⏳ Ожидание готовности PostgreSQL..."
sleep 5

docker-compose ps postgres

echo "✅ PostgreSQL запущен!"
echo "   Подключение: localhost:${POSTGRES_PORT:-5432}"
echo "   База данных: ${POSTGRES_DB:-ihome24}"
echo "   Пользователь: ${POSTGRES_USER:-iHome24db}"
