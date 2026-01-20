.PHONY: help start stop restart logs build clean ps shell-backend shell-postgres
.PHONY: start-postgres start-minio start-backend start-admin start-frontend
.PHONY: stop-postgres stop-minio stop-backend stop-admin stop-frontend
.PHONY: restart-postgres restart-minio restart-backend restart-admin restart-frontend
.PHONY: logs-postgres logs-minio logs-backend logs-admin logs-frontend
.PHONY: build-postgres build-minio build-backend build-admin build-frontend

help: ## Показать справку
	@echo "Доступные команды:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-25s\033[0m %s\n", $$1, $$2}'

## === Управление всеми сервисами ===

start: ## Запустить все сервисы
	@./start.sh

stop: ## Остановить все сервисы
	@./stop.sh

restart: ## Перезапустить все сервисы
	@./restart.sh

logs: ## Показать логи всех сервисов (использование: make logs SERVICE=backend)
	@./logs.sh $(SERVICE)

build: ## Пересобрать все образы
	docker-compose build --no-cache

up: ## Запустить в фоновом режиме
	docker-compose up -d

down: ## Остановить и удалить контейнеры
	docker-compose down

down-volumes: ## Остановить и удалить контейнеры с volumes
	docker-compose down -v

ps: ## Показать статус сервисов
	docker-compose ps

clean: ## Очистить неиспользуемые образы и volumes
	docker-compose down -v
	docker system prune -f

## === Управление PostgreSQL ===

start-postgres: ## Запустить PostgreSQL
	@./scripts/start-postgres.sh

stop-postgres: ## Остановить PostgreSQL
	@./scripts/stop-postgres.sh

restart-postgres: ## Перезапустить PostgreSQL
	docker-compose restart postgres

logs-postgres: ## Показать логи PostgreSQL
	docker-compose logs -f postgres

build-postgres: ## Пересобрать PostgreSQL (не требуется, используется готовый образ)

## === Управление MinIO ===

start-minio: ## Запустить MinIO
	@./scripts/start-minio.sh

stop-minio: ## Остановить MinIO
	@./scripts/stop-minio.sh

restart-minio: ## Перезапустить MinIO
	docker-compose restart minio

logs-minio: ## Показать логи MinIO
	docker-compose logs -f minio

build-minio: ## Пересобрать MinIO (не требуется, используется готовый образ)

## === Управление Backend ===

start-backend: ## Запустить Backend
	@./scripts/start-backend.sh

stop-backend: ## Остановить Backend
	@./scripts/stop-backend.sh

restart-backend: ## Перезапустить Backend
	docker-compose restart backend

logs-backend: ## Показать логи Backend
	docker-compose logs -f backend

build-backend: ## Пересобрать Backend
	docker-compose build --no-cache backend

## === Управление Admin Panel ===

start-admin: ## Запустить Admin Panel
	@./scripts/start-admin.sh

stop-admin: ## Остановить Admin Panel
	@./scripts/stop-admin.sh

restart-admin: ## Перезапустить Admin Panel
	docker-compose restart admin

logs-admin: ## Показать логи Admin Panel
	docker-compose logs -f admin

build-admin: ## Пересобрать Admin Panel
	docker-compose build --no-cache admin

## === Управление Frontend ===

start-frontend: ## Запустить Frontend
	@./scripts/start-frontend.sh

stop-frontend: ## Остановить Frontend
	@./scripts/stop-frontend.sh

restart-frontend: ## Перезапустить Frontend
	docker-compose restart frontend

logs-frontend: ## Показать логи Frontend
	docker-compose logs -f frontend

build-frontend: ## Пересобрать Frontend
	docker-compose build --no-cache frontend

## === Утилиты ===

shell-backend: ## Войти в shell бекенда
	docker-compose exec backend sh

shell-postgres: ## Войти в PostgreSQL shell
	docker-compose exec postgres psql -U ihome24 -d ihome24

shell-minio: ## Войти в shell MinIO
	docker-compose exec minio sh
