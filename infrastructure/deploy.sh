#!/bin/bash

# Скрипт для развертывания проекта в продакшене
# Использование: ./deploy.sh

set -e  # Остановка при ошибке

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функция для вывода сообщений
info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверка наличия необходимых файлов
check_files() {
    info "Проверка наличия необходимых файлов..."
    
    if [ ! -f "docker-compose.prod.yml" ]; then
        error "Файл docker-compose.prod.yml не найден!"
        exit 1
    fi
    
    if [ ! -f ".env.prod" ]; then
        error "Файл .env.prod не найден!"
        warn "Скопируйте env.prod.example в .env.prod и заполните переменные"
        exit 1
    fi
    
    info "Все необходимые файлы найдены ✓"
}

# Проверка переменных окружения
check_env() {
    info "Проверка переменных окружения..."
    
    source .env.prod
    
    if [ -z "$SPRING_DATASOURCE_URL" ]; then
        error "SPRING_DATASOURCE_URL не установлен в .env.prod"
        exit 1
    fi
    
    if [ -z "$ADMIN_API_BASE_URL" ] || [ -z "$FRONTEND_API_BASE_URL" ]; then
        error "API URLs не установлены в .env.prod"
        exit 1
    fi
    
    info "Переменные окружения проверены ✓"
}

# Сборка образов
build_images() {
    info "Сборка Docker образов..."
    docker compose -f docker-compose.prod.yml --env-file .env.prod build --no-cache
    info "Образы собраны ✓"
}

# Остановка старых контейнеров
stop_containers() {
    info "Остановка старых контейнеров..."
    docker compose -f docker-compose.prod.yml --env-file .env.prod down
    info "Контейнеры остановлены ✓"
}

# Запуск контейнеров
start_containers() {
    info "Запуск контейнеров..."
    docker compose -f docker-compose.prod.yml --env-file .env.prod up -d
    info "Контейнеры запущены ✓"
}

# Проверка статуса
check_status() {
    info "Проверка статуса контейнеров..."
    sleep 5
    docker compose -f docker-compose.prod.yml --env-file .env.prod ps
    
    info "Проверка health check..."
    sleep 10
    
    # Проверка backend health
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        info "Backend health check: OK ✓"
    else
        warn "Backend health check: FAILED"
    fi
    
    # Проверка frontend
    if curl -f http://localhost:3000 > /dev/null 2>&1; then
        info "Frontend доступен: OK ✓"
    else
        warn "Frontend недоступен"
    fi
    
    # Проверка admin
    if curl -f http://localhost:5173 > /dev/null 2>&1; then
        info "Admin доступен: OK ✓"
    else
        warn "Admin недоступен"
    fi
}

# Показать логи
show_logs() {
    info "Последние логи контейнеров:"
    docker compose -f docker-compose.prod.yml --env-file .env.prod logs --tail=50
}

# Главная функция
main() {
    info "Начало развертывания проекта iHome24..."
    
    check_files
    check_env
    build_images
    stop_containers
    start_containers
    check_status
    
    info "Развертывание завершено!"
    info "Для просмотра логов используйте: docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f"
    
    read -p "Показать последние логи? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        show_logs
    fi
}

# Запуск
main
