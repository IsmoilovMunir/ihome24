#!/bin/bash
# Просмотр логов в продакшене.
# Использование: ./logs-prod.sh [service]
# Без аргумента — логи всех сервисов. С аргументом — только указанный (frontend, backend, admin, postgres, minio).

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

if [ ! -f "$ENV_FILE" ]; then
    echo "Файл $ENV_FILE не найден."
    exit 1
fi

SERVICE="${1:-}"

if [ -z "$SERVICE" ]; then
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs -f
else
    docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" logs -f "$SERVICE"
fi
