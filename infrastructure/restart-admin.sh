#!/bin/bash

# Скрипт для перезапуска только Admin Panel

set -e

echo "🔄 Перезапуск Admin Panel..."

docker-compose restart admin

echo "✅ Admin Panel перезапущен!"
