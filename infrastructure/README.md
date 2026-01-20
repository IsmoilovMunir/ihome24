# Infrastructure - Docker Setup

Этот каталог содержит конфигурацию Docker для всего проекта iHome24.

## Структура

- `docker-compose.yml` - основной файл оркестрации всех сервисов
- `.env.example` - пример файла с переменными окружения
- `.dockerignore` - файлы, которые нужно игнорировать при сборке
- `start.sh` - скрипт для запуска всех сервисов
- `stop.sh` - скрипт для остановки всех сервисов
- `restart.sh` - скрипт для перезапуска всех сервисов
- `logs.sh` - скрипт для просмотра логов
- `Makefile` - альтернативный способ управления через make

## Сервисы

1. **PostgreSQL** (порт 5432) - база данных
2. **MinIO** (порты 9000, 9001) - объектное хранилище для файлов
3. **Backend** (порт 8080) - Spring Boot приложение
4. **Admin** (порт 5173) - админ-панель на Vue.js
5. **Frontend** (порт 3000) - пользовательский фронтенд на Vue.js

## Быстрый старт

### Вариант 1: Использование скриптов

1. Скопируйте `.env.example` в `.env` и при необходимости измените значения:
   ```bash
   cp .env.example .env
   ```

2. Запустите все сервисы:
   ```bash
   ./start.sh
   ```

3. Остановите все сервисы:
   ```bash
   ./stop.sh
   ```

### Вариант 2: Использование Makefile

```bash
make start    # Запустить все сервисы
make stop     # Остановить все сервисы
make restart  # Перезапустить все сервисы
make logs     # Показать логи (make logs SERVICE=backend)
make ps       # Показать статус сервисов
make build    # Пересобрать все образы
```

### Вариант 3: Использование docker-compose напрямую

```bash
docker-compose up -d
docker-compose ps
docker-compose logs -f
docker-compose down
```

## Остановка

```bash
./stop.sh
# или
make stop
# или
docker-compose down
```

Для удаления всех данных (включая volumes):
```bash
docker-compose down -v
```

## Переменные окружения

Все переменные настраиваются в файле `.env`. Основные:

- `POSTGRES_*` - настройки PostgreSQL
- `MINIO_*` - настройки MinIO
- `ADMIN_*` - настройки администратора по умолчанию
- `*_PORT` - порты для каждого сервиса

## Доступ к сервисам

- Backend API: http://localhost:8080
- Admin Panel: http://localhost:5173
- Frontend: http://localhost:3000
- MinIO Console: http://localhost:9001 (логин: minioadmin/minioadmin)
- PostgreSQL: localhost:5432

## Полезные команды

```bash
# Пересобрать и перезапустить все сервисы
docker-compose up -d --build

# Пересобрать только один сервис
docker-compose up -d --build backend

# Просмотр логов конкретного сервиса
docker-compose logs -f backend
make logs SERVICE=backend

# Выполнить команду в контейнере
docker-compose exec backend sh
docker-compose exec postgres psql -U ihome24 -d ihome24
make shell-backend
make shell-postgres

# Остановить все сервисы
docker-compose stop

# Запустить только базу данных и MinIO
docker-compose up -d postgres minio
make start-postgres
make start-minio

# Очистить все (контейнеры, volumes, образы)
make clean
```

## Управление отдельными сервисами

Каждый сервис запускается в **отдельном контейнере** и может управляться независимо.

### Через скрипты (рекомендуется)

```bash
# Запуск отдельных сервисов
./scripts/start-postgres.sh    # PostgreSQL
./scripts/start-minio.sh        # MinIO  
./scripts/start-backend.sh      # Backend (автоматически запустит PostgreSQL и MinIO)
./scripts/start-admin.sh        # Admin Panel (автоматически запустит Backend)
./scripts/start-frontend.sh     # Frontend (автоматически запустит Backend)

# Остановка отдельных сервисов
./scripts/stop-postgres.sh
./scripts/stop-minio.sh
./scripts/stop-backend.sh
./scripts/stop-admin.sh
./scripts/stop-frontend.sh
```

### Через Makefile

```bash
# Запуск отдельных сервисов
make start-postgres
make start-minio
make start-backend
make start-admin
make start-frontend

# Остановка отдельных сервисов
make stop-postgres
make stop-minio
make stop-backend
make stop-admin
make stop-frontend

# Перезапуск отдельных сервисов
make restart-backend
make restart-postgres

# Просмотр логов отдельных сервисов
make logs-backend
make logs-postgres
make logs-admin

# Пересборка отдельных сервисов
make build-backend
make build-admin
make build-frontend
```

### Через docker-compose напрямую

```bash
# Запуск одного сервиса
docker-compose up -d postgres
docker-compose up -d minio
docker-compose up -d backend
docker-compose up -d admin
docker-compose up -d frontend

# Остановка одного сервиса
docker-compose stop postgres
docker-compose stop backend

# Перезапуск одного сервиса
docker-compose restart backend

# Просмотр логов одного сервиса
docker-compose logs -f backend
docker-compose logs -f postgres

# Пересборка одного сервиса
docker-compose build backend
docker-compose up -d --build backend
```

### Порядок запуска

Рекомендуемый порядок запуска (скрипты автоматически проверяют зависимости):

1. **PostgreSQL** - база данных (не зависит ни от чего)
2. **MinIO** - хранилище файлов (не зависит ни от чего)
3. **Backend** - требует PostgreSQL и MinIO
4. **Admin Panel** - требует Backend
5. **Frontend** - требует Backend

Подробнее см. [scripts/README.md](scripts/README.md)

## Требования

- Docker 20.10+
- Docker Compose 2.0+
- Минимум 4GB свободной RAM
- Минимум 10GB свободного места на диске

## Troubleshooting

### Проблемы с портами

Если порты заняты, измените их в файле `.env`:
```bash
BACKEND_PORT=8081
ADMIN_PORT=5174
FRONTEND_PORT=3001
```

### Проблемы с базой данных

Если база данных не запускается:
```bash
docker-compose logs postgres
docker-compose exec postgres psql -U ihome24 -d ihome24
```

### Пересборка после изменений

После изменения кода пересоберите образы:
```bash
docker-compose build --no-cache backend
docker-compose up -d backend
```
