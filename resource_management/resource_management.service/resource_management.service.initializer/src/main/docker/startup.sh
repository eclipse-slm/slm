#! /bin/bash

set -euo pipefail

COMMON_SH="/app/common.sh"
source "$COMMON_SH"

wait_for_keycloak
wait_for_resource_management
start_app