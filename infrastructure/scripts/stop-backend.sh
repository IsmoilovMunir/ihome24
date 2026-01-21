#!/bin/bash

# Скрипт для остановки Backend

echo "🛑 Остановка Backend..."

docker-compose stop backend

echo "✅ Backend остановлен!"
