#!/bin/bash

# Скрипт для остановки PostgreSQL

echo "🛑 Остановка PostgreSQL..."

docker-compose stop postgres

echo "✅ PostgreSQL остановлен!"
