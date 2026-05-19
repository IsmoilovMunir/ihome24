# Подключить в bash-скриптах: source "$(dirname "$0")/load-env.sh"
#
# 1) backend/.env — секреты Spring (MAIL, TELEGRAM, DADATA, …)
# 2) infrastructure/.env — опционально: порты Docker, Postgres, MinIO

_load_env_file() {
  local file="$1"
  if [ -f "$file" ]; then
    set -a
    # shellcheck disable=SC1090
    source "$file"
    set +a
    echo "✓ $(basename "$file") ← $file"
    return 0
  fi
  return 1
}

# Корень репозитория (infrastructure/..)
if [ -n "${BASH_SOURCE[0]:-}" ]; then
  _LOAD_ENV_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
else
  _LOAD_ENV_SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
fi
REPO_ROOT="$(cd "$_LOAD_ENV_SCRIPT_DIR/../.." && pwd)"
INFRA_DIR="$(cd "$_LOAD_ENV_SCRIPT_DIR/.." && pwd)"

_load_env_file "$REPO_ROOT/backend/.env" || true
_load_env_file "$INFRA_DIR/.env" || true
