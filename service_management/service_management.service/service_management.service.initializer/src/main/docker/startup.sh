#! /bin/bash

set -euo pipefail

COMMON_SH="/app/common.sh"
source "$COMMON_SH"

echo "Init Directory: $SERVICEMANAGEMENT_INITDIRECTORIES"

wait_for_keycloak
wait_for_service_management
start_app
