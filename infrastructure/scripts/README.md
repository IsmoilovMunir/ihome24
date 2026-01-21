# Скрипты управления отдельными сервисами

Этот каталог содержит скрипты для управления каждым сервисом отдельно.

## Запуск отдельных сервисов

```bash
# PostgreSQL
./scripts/start-postgres.sh

# MinIO
./scripts/start-minio.sh

# Backend
./scripts/start-backend.sh

# Admin Panel
./scripts/start-admin.sh

# Frontend
./scripts/start-frontend.sh
```

## Остановка отдельных сервисов

```bash
# PostgreSQL
./scripts/stop-postgres.sh

# MinIO
./scripts/stop-minio.sh

# Backend
./scripts/stop-backend.sh

# Admin Panel
./scripts/stop-admin.sh

# Frontend
./scripts/stop-frontend.sh
```

## Использование через docker-compose напрямую

Вы также можете использовать docker-compose напрямую:

```bash
# Запуск одного сервиса
docker-compose up -d postgres
docker-compose up -d minio
docker-compose up -d backend
docker-compose up -d admin
docker-compose up -d frontend

# Остановка одного сервиса
docker-compose stop postgres
docker-compose stop minio
docker-compose stop backend
docker-compose stop admin
docker-compose stop frontend

# Перезапуск одного сервиса
docker-compose restart postgres
docker-compose restart backend

# Просмотр логов одного сервиса
docker-compose logs -f backend
docker-compose logs -f postgres

# Пересборка одного сервиса
docker-compose build backend
docker-compose up -d --build backend
```

## Порядок запуска

Рекомендуемый порядок запуска:

1. **PostgreSQL** - база данных (не зависит ни от чего)
2. **MinIO** - хранилище файлов (не зависит ни от чего)
3. **Backend** - требует PostgreSQL и MinIO
4. **Admin Panel** - требует Backend
5. **Frontend** - требует Backend

Скрипты автоматически проверяют зависимости и запускают их при необходимости.
