#!/bin/bash

# Скрипт для запуска всех сервисов

set -e

echo "🚀 Запуск iHome24 Infrastructure..."

# Проверяем наличие .env файла
if [ ! -f .env ]; then
    echo "⚠️  Файл .env не найден. Создаю из .env.example..."
    cp .env.example .env
    echo "✅ Файл .env создан. Проверьте настройки перед запуском."
fi

# Создаем директорию для логов если её нет
mkdir -p logs/backend

# Запускаем все сервисы
echo "📦 Запуск Docker Compose..."
docker-compose up -d --build

echo "⏳ Ожидание готовности сервисов..."
sleep 10

# Проверяем статус
echo ""
echo "📊 Статус сервисов:"
docker-compose ps

echo ""
echo "✅ Все сервисы запущены!"
echo ""
echo "🌐 Доступные сервисы:"
echo "   - Backend API: http://localhost:8080"
echo "   - Admin Panel: http://localhost:5173"
echo "   - Frontend: http://localhost:3000"
echo "   - MinIO Console: http://localhost:9001"
echo "   - PostgreSQL: localhost:5432"
echo ""
echo "📝 Просмотр логов: docker-compose logs -f [service_name]"
echo "🛑 Остановка: docker-compose down"
