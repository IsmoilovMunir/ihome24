#!/bin/bash

# Скрипт для остановки MinIO

echo "🛑 Остановка MinIO..."

docker-compose stop minio minio-setup

echo "✅ MinIO остановлен!"
