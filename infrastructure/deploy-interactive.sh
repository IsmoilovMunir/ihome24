#!/bin/bash

# Интерактивный скрипт развертывания на удаленном сервере
# Использование: ./deploy-interactive.sh

set -e

# Цвета
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

question() {
    echo -e "${BLUE}[?]${NC} $1"
}

# Проверка, что скрипт запущен на сервере
check_server() {
    if [ ! -f "/opt/ihome24/infrastructure/docker-compose.prod.yml" ]; then
        error "Этот скрипт должен быть запущен на сервере в директории /opt/ihome24/infrastructure"
        exit 1
    fi
}

# Шаг 1: Проверка системы
step1_check_system() {
    info "Шаг 1: Проверка системы..."
    
    # Проверка Docker
    if ! command -v docker &> /dev/null; then
        error "Docker не установлен!"
        warn "Установите Docker: curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh"
        exit 1
    fi
    info "✓ Docker установлен: $(docker --version)"
    
    # Проверка Docker Compose
    if ! docker compose version &> /dev/null; then
        error "Docker Compose не установлен!"
        exit 1
    fi
    info "✓ Docker Compose установлен: $(docker compose version)"
    
    # Проверка Nginx
    if ! command -v nginx &> /dev/null; then
        error "Nginx не установлен!"
        warn "Установите Nginx: sudo apt-get install -y nginx"
        exit 1
    fi
    info "✓ Nginx установлен"
    
    # Проверка места на диске
    available_space=$(df -h / | awk 'NR==2 {print $4}')
    info "✓ Свободное место: $available_space"
}

# Шаг 2: Проверка файлов проекта
step2_check_files() {
    info "Шаг 2: Проверка файлов проекта..."
    
    if [ ! -f "docker-compose.prod.yml" ]; then
        error "Файл docker-compose.prod.yml не найден!"
        exit 1
    fi
    info "✓ docker-compose.prod.yml найден"
    
    if [ ! -f "env.prod.example" ]; then
        error "Файл env.prod.example не найден!"
        exit 1
    fi
    info "✓ env.prod.example найден"
    
    if [ ! -f ".env.prod" ]; then
        warn "Файл .env.prod не найден!"
        question "Создать .env.prod из env.prod.example? (y/n)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            cp env.prod.example .env.prod
            info "✓ Файл .env.prod создан"
            warn "ВАЖНО: Отредактируйте .env.prod и заполните все переменные перед продолжением!"
            question "Открыть .env.prod для редактирования? (y/n)"
            read -r edit_response
            if [[ "$edit_response" =~ ^[Yy]$ ]]; then
                ${EDITOR:-nano} .env.prod
            fi
        else
            error "Необходимо создать .env.prod перед продолжением"
            exit 1
        fi
    else
        info "✓ .env.prod найден"
    fi
    
    # Проверка прав доступа
    chmod 600 .env.prod
    info "✓ Права доступа на .env.prod установлены"
}

# Шаг 3: Проверка переменных окружения
step3_check_env() {
    info "Шаг 3: Проверка переменных окружения..."
    
    source .env.prod
    
    local errors=0
    
    if [ -z "$SPRING_DATASOURCE_URL" ] || [[ "$SPRING_DATASOURCE_URL" == *"your-db-host"* ]]; then
        error "SPRING_DATASOURCE_URL не настроен!"
        errors=$((errors + 1))
    else
        info "✓ SPRING_DATASOURCE_URL настроен"
    fi
    
    if [ -z "$ADMIN_API_BASE_URL" ] || [[ "$ADMIN_API_BASE_URL" == *"yourdomain.com"* ]]; then
        error "ADMIN_API_BASE_URL не настроен (должен содержать ваш домен)!"
        errors=$((errors + 1))
    else
        info "✓ ADMIN_API_BASE_URL настроен"
    fi
    
    if [ -z "$FRONTEND_API_BASE_URL" ] || [[ "$FRONTEND_API_BASE_URL" == *"yourdomain.com"* ]]; then
        error "FRONTEND_API_BASE_URL не настроен (должен содержать ваш домен)!"
        errors=$((errors + 1))
    else
        info "✓ FRONTEND_API_BASE_URL настроен"
    fi
    
    if [ -z "$CORS_ALLOWED_ORIGINS" ] || [[ "$CORS_ALLOWED_ORIGINS" == *"yourdomain.com"* ]]; then
        error "CORS_ALLOWED_ORIGINS не настроен (должен содержать ваш домен)!"
        errors=$((errors + 1))
    else
        info "✓ CORS_ALLOWED_ORIGINS настроен"
    fi
    
    if [ $errors -gt 0 ]; then
        error "Найдено $errors ошибок в переменных окружения!"
        warn "Отредактируйте .env.prod и запустите скрипт снова"
        exit 1
    fi
}

# Шаг 4: Проверка Nginx
step4_check_nginx() {
    info "Шаг 4: Проверка Nginx..."
    
    if [ ! -f "/etc/nginx/sites-available/ihome24" ]; then
        warn "Конфигурация Nginx не найдена!"
        question "Скопировать конфигурацию из проекта? (y/n)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            question "Использовать вариант с поддоменами? (y/n, n = один домен с путями)"
            read -r subdomain_response
            if [[ "$subdomain_response" =~ ^[Yy]$ ]]; then
                sudo cp nginx/nginx.conf /etc/nginx/sites-available/ihome24
            else
                sudo cp nginx/nginx-alternative.conf /etc/nginx/sites-available/ihome24
            fi
            
            question "Введите ваш домен (например, ihome24.ru):"
            read -r domain
            sudo sed -i "s/ihome24.ru/$domain/g" /etc/nginx/sites-available/ihome24
            sudo sed -i "s/www.ihome24.ru/www.$domain/g" /etc/nginx/sites-available/ihome24
            sudo sed -i "s/admin.ihome24.ru/admin.$domain/g" /etc/nginx/sites-available/ihome24
            sudo sed -i "s/api.ihome24.ru/api.$domain/g" /etc/nginx/sites-available/ihome24
            
            sudo ln -sf /etc/nginx/sites-available/ihome24 /etc/nginx/sites-enabled/
            sudo rm -f /etc/nginx/sites-enabled/default
            
            if sudo nginx -t; then
                sudo systemctl restart nginx
                info "✓ Nginx настроен и перезапущен"
            else
                error "Ошибка в конфигурации Nginx!"
                exit 1
            fi
        else
            error "Необходимо настроить Nginx перед продолжением"
            exit 1
        fi
    else
        info "✓ Конфигурация Nginx найдена"
        if sudo nginx -t; then
            info "✓ Конфигурация Nginx валидна"
        else
            error "Ошибка в конфигурации Nginx!"
            exit 1
        fi
    fi
}

# Шаг 5: Сборка образов
step5_build_images() {
    info "Шаг 5: Сборка Docker образов..."
    warn "Это может занять 10-30 минут..."
    
    question "Продолжить сборку? (y/n)"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        warn "Сборка отменена"
        exit 0
    fi
    
    docker compose -f docker-compose.prod.yml --env-file .env.prod build
    
    if [ $? -eq 0 ]; then
        info "✓ Образы собраны успешно"
    else
        error "Ошибка при сборке образов!"
        exit 1
    fi
}

# Шаг 6: Запуск контейнеров
step6_start_containers() {
    info "Шаг 6: Запуск контейнеров..."
    
    docker compose -f docker-compose.prod.yml --env-file .env.prod up -d
    
    if [ $? -eq 0 ]; then
        info "✓ Контейнеры запущены"
    else
        error "Ошибка при запуске контейнеров!"
        exit 1
    fi
    
    sleep 5
    
    info "Статус контейнеров:"
    docker compose -f docker-compose.prod.yml --env-file .env.prod ps
}

# Шаг 7: Проверка работоспособности
step7_check_health() {
    info "Шаг 7: Проверка работоспособности..."
    
    sleep 10
    
    # Backend
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        info "✓ Backend доступен"
    else
        warn "⚠ Backend недоступен (проверьте логи)"
    fi
    
    # Frontend
    if curl -f http://localhost:3000 > /dev/null 2>&1; then
        info "✓ Frontend доступен"
    else
        warn "⚠ Frontend недоступен (проверьте логи)"
    fi
    
    # Admin
    if curl -f http://localhost:5173 > /dev/null 2>&1; then
        info "✓ Admin доступен"
    else
        warn "⚠ Admin недоступен (проверьте логи)"
    fi
}

# Шаг 8: SSL сертификат
step8_ssl() {
    info "Шаг 8: Настройка SSL сертификата..."
    
    question "Настроить SSL сертификат через Let's Encrypt? (y/n)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        source .env.prod
        
        # Извлекаем домены из CORS_ALLOWED_ORIGINS
        domains=$(echo "$CORS_ALLOWED_ORIGINS" | sed 's/https:\/\///g' | sed 's/,/ /g' | awk '{print $1}' | head -1)
        main_domain=$(echo "$domains" | awk '{print $1}')
        
        question "Основной домен: $main_domain (правильно?) (y/n)"
        read -r domain_confirm
        if [[ ! "$domain_confirm" =~ ^[Yy]$ ]]; then
            question "Введите основной домен:"
            read -r main_domain
        fi
        
        question "Использовать поддомены? (admin.$main_domain, api.$main_domain) (y/n)"
        read -r subdomain_ssl
        if [[ "$subdomain_ssl" =~ ^[Yy]$ ]]; then
            sudo certbot --nginx -d "$main_domain" -d "www.$main_domain" -d "admin.$main_domain" -d "api.$main_domain"
        else
            sudo certbot --nginx -d "$main_domain" -d "www.$main_domain"
        fi
        
        if [ $? -eq 0 ]; then
            info "✓ SSL сертификат установлен"
        else
            warn "⚠ Ошибка при установке SSL сертификата"
        fi
    else
        warn "SSL сертификат не настроен. Настройте его позже командой: sudo certbot --nginx -d yourdomain.com"
    fi
}

# Главная функция
main() {
    echo "=========================================="
    echo "  Развертывание проекта iHome24"
    echo "=========================================="
    echo ""
    
    check_server
    
    step1_check_system
    echo ""
    
    step2_check_files
    echo ""
    
    step3_check_env
    echo ""
    
    step4_check_nginx
    echo ""
    
    step5_build_images
    echo ""
    
    step6_start_containers
    echo ""
    
    step7_check_health
    echo ""
    
    step8_ssl
    echo ""
    
    info "=========================================="
    info "  Развертывание завершено!"
    info "=========================================="
    info ""
    info "Проверьте доступность:"
    source .env.prod
    domains=$(echo "$CORS_ALLOWED_ORIGINS" | sed 's/https:\/\///g' | sed 's/,/ /g')
    for domain in $domains; do
        info "  - https://$domain"
    done
    info ""
    info "Полезные команды:"
    info "  - Логи: docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f"
    info "  - Статус: docker compose -f docker-compose.prod.yml --env-file .env.prod ps"
    info "  - Перезапуск: docker compose -f docker-compose.prod.yml --env-file .env.prod restart"
}

main
