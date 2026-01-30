#!/bin/bash

# Скрипт для обновления проекта в продакшене
# Использование: ./update.sh [service]
# Если service не указан, обновляются все сервисы

set -e

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

SERVICE=${1:-all}

# Проверка файлов
if [ ! -f "docker-compose.prod.yml" ] || [ ! -f ".env.prod" ]; then
    error "Необходимые файлы не найдены!"
    exit 1
fi

info "Обновление проекта iHome24..."

if [ "$SERVICE" = "all" ]; then
    info "Обновление всех сервисов..."
    
    # Получаем последние изменения из git (если используется git)
    if [ -d "../.git" ]; then
        info "Получение последних изменений из git..."
        cd ..
        git pull
        cd infrastructure
    fi
    
    # Пересборка всех образов
    info "Пересборка образов..."
    docker compose -f docker-compose.prod.yml --env-file .env.prod build --no-cache
    
    # Перезапуск всех контейнеров
    info "Перезапуск контейнеров..."
    docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --force-recreate
    
else
    info "Обновление сервиса: $SERVICE"
    
    # Пересборка конкретного сервиса
    docker compose -f docker-compose.prod.yml --env-file .env.prod build --no-cache $SERVICE
    
    # Перезапуск конкретного сервиса
    docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --no-deps --force-recreate $SERVICE
fi

info "Обновление завершено!"
info "Проверка статуса..."
sleep 5
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
