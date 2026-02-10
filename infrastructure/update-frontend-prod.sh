#!/bin/bash
# Обновление только фронтенда в продакшене.
# Подтягивает код из git, пересобирает образ frontend и пересоздаёт контейнер.
# Использование: ./update-frontend-prod.sh

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
exec ./update.sh frontend
