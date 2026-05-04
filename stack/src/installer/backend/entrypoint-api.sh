#!/usr/bin/env bash
set -euo pipefail

FIXED_SLM_VERSION="${INSTALLER_FIXED_SLM_VERSION:-1.5.0-SNAPSHOT}"
INSTALLER_WORKING_DIR="${INSTALLER_WORKING_DIR:-/ansible/installer/src}"
INSTALLER_PLAYBOOK="${INSTALLER_PLAYBOOK:-main.yml}"
INSTALLER_UNINSTALL_WORKING_DIR="${INSTALLER_UNINSTALL_WORKING_DIR:-/ansible/uninstaller/src}"
INSTALLER_UNINSTALL_PLAYBOOK="${INSTALLER_UNINSTALL_PLAYBOOK:-main.yml}"
INSTALLER_ENV_FILE="${INSTALLER_ENV_FILE:-/env/env.yml}"
INSTALLER_API_HOST="${INSTALLER_API_HOST:-0.0.0.0}"
INSTALLER_API_PORT="${INSTALLER_API_PORT:-8080}"

PYTHON_BIN="${PYTHON_BIN:-python3}"
if ! command -v "$PYTHON_BIN" >/dev/null 2>&1; then
  PYTHON_BIN="python"
fi

if ! command -v "$PYTHON_BIN" >/dev/null 2>&1; then
  echo "Python runtime not found in container" >&2
  exit 127
fi

build_execution_env_json() {
  local mode="$1"

  "$PYTHON_BIN" - "$mode" <<'PY'
import json
import os
import sys

mode = sys.argv[1]
payload = {
    "SLM_VERSION": os.getenv("INSTALLER_FIXED_SLM_VERSION", "1.5.0-SNAPSHOT")
}

if mode == "install":
    missing = [name for name in ("SLM_HOSTNAME", "SLM_IP") if not os.getenv(name)]
    if missing:
        print(
            "Missing required environment variables for install mode: " + ", ".join(missing),
            file=sys.stderr,
        )
        sys.exit(1)

    payload["SLM_HOSTNAME"] = os.environ["SLM_HOSTNAME"]
    payload["SLM_IP"] = os.environ["SLM_IP"]

print(json.dumps(payload))
PY
}

run_non_interactive() {
  local mode="$1"
  local working_dir=""
  local playbook=""

  case "$mode" in
    install)
      working_dir="$INSTALLER_WORKING_DIR"
      playbook="$INSTALLER_PLAYBOOK"
      ;;
    uninstall)
      working_dir="$INSTALLER_UNINSTALL_WORKING_DIR"
      playbook="$INSTALLER_UNINSTALL_PLAYBOOK"
      ;;
    *)
      echo "Unsupported container mode: $mode" >&2
      exit 2
      ;;
  esac

  local execution_env
  execution_env="$(build_execution_env_json "$mode")"

  exec "$PYTHON_BIN" -m backend.cli_runner \
    --job-id "container-${mode}" \
    --working-dir "$working_dir" \
    --playbook "$playbook" \
    --env-file "$INSTALLER_ENV_FILE" \
    --execution-env "$execution_env"
}

start_api() {
  exec "$PYTHON_BIN" -m uvicorn backend.main:app --host "$INSTALLER_API_HOST" --port "$INSTALLER_API_PORT" "$@"
}

case "${1:-}" in
  install)
    shift
    if [ "$#" -gt 0 ]; then
      echo "Install mode does not accept additional arguments" >&2
      exit 2
    fi
    run_non_interactive install
    ;;
  uninstall)
    shift
    if [ "$#" -gt 0 ]; then
      echo "Uninstall mode does not accept additional arguments" >&2
      exit 2
    fi
    run_non_interactive uninstall
    ;;
  "")
    start_api
    ;;
  *)
    echo "Unsupported container mode: ${1}. Use no argument, 'install', or 'uninstall'." >&2
    exit 2
    ;;
esac

