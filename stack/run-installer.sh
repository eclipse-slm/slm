#!/usr/bin/env bash
set -euo pipefail

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

: "${SLM_HOSTNAME:?Error: SLM_HOSTNAME is not set}"
: "${SLM_IP:?Error: SLM_IP is not set}"
: "${SLM_VERSION:?Error: SLM_VERSION is not set}"

# Disable rsyslog AppArmor profile only on Ubuntu 24+ hosts.
if [[ -r /etc/os-release ]]; then
  . /etc/os-release
  os_major="${VERSION_ID%%.*}"
  if [[ "${ID:-}" == "ubuntu" && "${os_major}" =~ ^[0-9]+$ && "${os_major}" -ge 24 ]]; then
    sudo ln -sf /etc/apparmor.d/usr.sbin.rsyslogd /etc/apparmor.d/disable/
    sudo apparmor_parser -R /etc/apparmor.d/usr.sbin.rsyslogd
  fi
fi

docker run \
  --rm \
  --name eclipse-slm-installer \
  --pull=always \
  --env "SLM_HOSTNAME=${SLM_HOSTNAME}" \
  --env "SLM_IP=${SLM_IP}" \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  --add-host "${SLM_HOSTNAME}:host-gateway" \
  ghcr.io/eclipse-slm/slm/installer:${SLM_VERSION}
