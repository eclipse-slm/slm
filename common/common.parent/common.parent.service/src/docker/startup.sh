#! /bin/bash

set -euo pipefail

COMMON_SH="/app/common.sh"
source "$COMMON_SH"

wait_for_vault
import_root_ca
wait_for_consul
start_app
