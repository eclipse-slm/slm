#!/usr/bin/env bash
set -euo pipefail

MODE_ARG=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --mode)
      if [[ $# -lt 2 ]]; then
        echo "Error: --mode requires a value (ui|install|uninstall)." >&2
        exit 1
      fi
      MODE_ARG="$2"
      shift 2
      ;;
    *)
      echo "Error: unsupported argument '$1'. Use --mode ui|install|uninstall." >&2
      exit 1
      ;;
  esac
done

INSTALLER_MODE="${MODE_ARG:-${INSTALLER_MODE:-}}"
INSTALLER_DOWNLOAD_REF="${INSTALLER_DOWNLOAD_REF:-develop}"
INSTALLER_COMPOSE_PROJECT_NAME="eclipse-slm-installer"
SLM_VERSION="1.5.0-SNAPSHOT"
INSTALL_DIRECTORY="${INSTALL_DIRECTORY:-/opt/eclipse-slm}"
DEPLOYMENT_HOST_HOSTNAME="${DEPLOYMENT_HOST_HOSTNAME:-localhost}"
DEPLOYMENT_HOST_USER="${DEPLOYMENT_HOST_USER:-}"
DEPLOYMENT_HOST_PASSWORD="${DEPLOYMENT_HOST_PASSWORD:-}"

echo "Eclipse Service Lifecycle Management | Installer | Version: ${SLM_VERSION}"

if [[ -z "${INSTALLER_MODE}" ]]; then
  echo "Select installer mode:"
  echo "  1) UI mode (with web interface)"
  echo "  2) Install (CLI)"
  echo "  3) Uninstall (CLI)"
  read -r -p "Enter choice [1-3]: " mode_choice
  case "${mode_choice}" in
    1) INSTALLER_MODE="ui" ;;
    2) INSTALLER_MODE="install" ;;
    3) INSTALLER_MODE="uninstall" ;;
    *)
      echo "Error: invalid selection '${mode_choice}'. Expected 1, 2, or 3." >&2
      exit 1
      ;;
  esac
fi

case "${INSTALLER_MODE}" in
  ui|install|uninstall) ;;
  *)
    echo "Error: invalid mode '${INSTALLER_MODE}'. Allowed values: ui, install, uninstall." >&2
    exit 1
    ;;
esac

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker command not found in PATH." >&2
  exit 1
fi

if [[ "${EUID}" -ne 0 ]]; then
  if ! command -v sudo >/dev/null 2>&1; then
    echo "Error: sudo command not found, but this script requires elevated privileges." >&2
    exit 1
  fi

  if ! sudo -v; then
    echo "Error: current user does not have sudo privileges required by this script." >&2
    exit 1
  fi
fi

# Disable rsyslog AppArmor profile only on Ubuntu 24+ hosts.
if [[ -r /etc/os-release ]]; then
  . /etc/os-release
  os_major="${VERSION_ID%%.*}"
  if [[ "${ID:-}" == "ubuntu" && "${os_major}" =~ ^[0-9]+$ && "${os_major}" -ge 24 ]]; then
    sudo ln -sf /etc/apparmor.d/usr.sbin.rsyslogd /etc/apparmor.d/disable/
    sudo apparmor_parser -R /etc/apparmor.d/usr.sbin.rsyslogd
  fi
fi

prompt_required_with_default() {
  local var_name="$1"
  local prompt_label="$2"
  local current_value="${!var_name-}"
  local entered_value=""

  while true; do
    if [[ -n "${current_value}" ]]; then
      read -r -p "${prompt_label} [${current_value}]: " entered_value
      if [[ -z "${entered_value}" ]]; then
        entered_value="${current_value}"
      fi
    else
      read -r -p "${prompt_label}: " entered_value
    fi

    if [[ -n "${entered_value}" ]]; then
      printf -v "${var_name}" '%s' "${entered_value}"
      break
    fi

    echo "Error: ${prompt_label} must not be empty." >&2
  done
}

if [[ -z "${INSTALL_DIRECTORY}" ]]; then
  prompt_required_with_default "INSTALL_DIRECTORY" "INSTALL_DIRECTORY"
fi

if [[ "${INSTALLER_MODE}" == "ui" ]]; then
  if ! docker compose version >/dev/null 2>&1; then
    echo "Error: 'docker compose' is not available. Please install Docker Compose v2." >&2
    exit 1
  fi

  TMP_DIR="$(mktemp -d)"
  COMPOSE_FILE="${TMP_DIR}/docker-compose.yml"
  ENV_FILE="${TMP_DIR}/.env"
  COMPOSE_URL="https://raw.githubusercontent.com/eclipse-slm/slm/${INSTALLER_DOWNLOAD_REF}/stack/installer/docker-compose.yml"

  cleanup_tmp_dir() {
    rm -rf "${TMP_DIR}"
  }
  trap cleanup_tmp_dir EXIT

  if ! curl -fsSL "${COMPOSE_URL}" -o "${COMPOSE_FILE}"; then
    echo "Error: failed to download docker-compose.yml from '${COMPOSE_URL}'." >&2
    exit 1
  fi

  cat > "${ENV_FILE}" <<EOF
SLM_HOSTNAME=${SLM_HOSTNAME:-}
SLM_IP=${SLM_IP:-}
SLM_VERSION=${SLM_VERSION}
INSTALL_DIRECTORY=${INSTALL_DIRECTORY}
DEPLOYMENT_HOST_HOSTNAME=${DEPLOYMENT_HOST_HOSTNAME}
DEPLOYMENT_HOST_USER=${DEPLOYMENT_HOST_USER}
DEPLOYMENT_HOST_PASSWORD=${DEPLOYMENT_HOST_PASSWORD}
EOF

  sudo docker compose \
    -p "${INSTALLER_COMPOSE_PROJECT_NAME}" \
    --env-file "${ENV_FILE}" \
    -f "${COMPOSE_FILE}" \
    up -d --pull always --remove-orphans

  echo "Open installer UI with one of these URLs:"
  echo "  http://localhost:6060"
  host_ips="$(hostname -I 2>/dev/null | tr ' ' '\n' | sed '/^$/d' | sort -u)"
  if [[ -n "${host_ips}" ]]; then
    while IFS= read -r ip; do
      # Skip typical Docker bridge gateway addresses like 172.17.0.1, 172.18.0.1, ...
      if [[ "${ip}" =~ ^172\.(1[7-9]|2[0-9]|3[0-1])\.0\.1$ ]]; then
        continue
      fi
      echo "  http://${ip}:6060"
    done <<< "${host_ips}"
  fi
  exit 0
fi

if [[ "${INSTALLER_MODE}" == "install" ]]; then
  prompt_required_with_default "SLM_HOSTNAME" "SLM_HOSTNAME"
  prompt_required_with_default "SLM_IP" "SLM_IP"
fi

echo "Starting non-interactive installer with:"
echo "MODE=${INSTALLER_MODE}"
echo "INSTALL_DIRECTORY=${INSTALL_DIRECTORY}"
if [[ "${INSTALLER_MODE}" == "install" ]]; then
  echo "SLM_HOSTNAME=${SLM_HOSTNAME}"
  echo "SLM_IP=${SLM_IP}"
  echo "DEPLOYMENT_HOST_HOSTNAME=${DEPLOYMENT_HOST_HOSTNAME}"
  echo "DEPLOYMENT_HOST_USER=${DEPLOYMENT_HOST_USER}"
fi
echo "SLM_VERSION=${SLM_VERSION}"

if [[ "${INSTALLER_MODE}" == "install" ]]; then
  sudo docker run \
    --rm \
    --pull=always \
    --env "SLM_HOSTNAME=${SLM_HOSTNAME}" \
    --env "SLM_IP=${SLM_IP}" \
    --env "INSTALL_DIRECTORY=${INSTALL_DIRECTORY}" \
    --env "DEPLOYMENT_HOST_HOSTNAME=${DEPLOYMENT_HOST_HOSTNAME}" \
    --env "DEPLOYMENT_HOST_USER=${DEPLOYMENT_HOST_USER}" \
    --env "DEPLOYMENT_HOST_PASSWORD=${DEPLOYMENT_HOST_PASSWORD}" \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${INSTALL_DIRECTORY}:/install" \
    --add-host "${SLM_HOSTNAME}:host-gateway" \
    ghcr.io/eclipse-slm/slm/installer-api:${SLM_VERSION} \
    install
else
  sudo docker run \
    --rm \
    --pull=always \
    --env "INSTALL_DIRECTORY=${INSTALL_DIRECTORY}" \
    --volume /var/run/docker.sock:/var/run/docker.sock \
    --volume "${INSTALL_DIRECTORY}:/install" \
    ghcr.io/eclipse-slm/slm/installer-api:${SLM_VERSION} \
    uninstall

  # Remove installer-generated files after successful uninstall
  sudo rm -rf "${INSTALL_DIRECTORY}"
fi
