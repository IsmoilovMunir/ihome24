#!/bin/bash
# Пометить все changeset'ы как выполненные (для prod-БД, созданной без Liquibase).
# Запуск из infrastructure/: ./scripts/liquibase-changelog-sync.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_ROOT="$(cd "$INFRA_DIR/.." && pwd)"
ENV_FILE="${INFRA_DIR}/.env.prod"

if [ ! -f "$ENV_FILE" ]; then
  echo "Не найден $ENV_FILE"
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

NETWORK="${COMPOSE_PROJECT_NAME:-infrastructure}_ihome24-network"
if ! docker network inspect "$NETWORK" >/dev/null 2>&1; then
  NETWORK="ihome24-network"
fi

DB_NAME="${POSTGRES_DB:-ihome24}"
DB_USER="${SPRING_DATASOURCE_USERNAME:-iHome24db}"
DB_PASS="${SPRING_DATASOURCE_PASSWORD:?Задайте SPRING_DATASOURCE_PASSWORD в .env.prod}"

echo "Liquibase changelog-sync → postgres:5432/$DB_NAME (network: $NETWORK)"

# Не монтировать в /liquibase — перезапишет entrypoint образа
docker run --rm \
  --network "$NETWORK" \
  -v "$REPO_ROOT/backend/src/main/resources:/work" \
  liquibase/liquibase:4.29 \
  --url="jdbc:postgresql://postgres:5432/${DB_NAME}" \
  --username="$DB_USER" \
  --password="$DB_PASS" \
  --searchPath=/work \
  --changeLogFile=db/changelog/db.changelog-master.yaml \
  changelog-sync

echo "Готово. Проверка:"
docker exec ihome24-postgres-prod psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) AS changesets FROM databasechangelog;"
