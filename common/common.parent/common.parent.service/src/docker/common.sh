#! /bin/bash

###region Variables & Config
set -euo pipefail

VAULT_HEALTH_URL="http://vault:8200/v1/sys/health"
CONSUL_URL="${CONSUL_SCHEME}://${CONSUL_HOST}:${CONSUL_PORT}"
###endregion Variables & Config

###region Methods
wait_for_vault() {
  while true; do
    local vault_status
    vault_status=$(curl -s -o /dev/null -w "%{http_code}" "${VAULT_HEALTH_URL}" || true)
    if [ "$vault_status" = "200" ]; then
      break
    fi
    echo "[ Waiting for Vault at ${VAULT_HEALTH_URL} ... ]"
    sleep 3
  done
}

import_root_ca() {
  echo "[ Get SLM Root CA cert from Vault and add it to Java trust store ... ]"
  local ca_file="/vault/slm-root-ca.crt"
  local keystore_pass="changeit"
  local alias_name="slm-root-ca"

  mkdir -p /vault
  curl -s http://vault:8200/v1/pki_root_slm/cert/ca | jq -r .data.certificate > "$ca_file"

  # Delete existing alias if it exists
  if keytool -list -cacerts -storepass "$keystore_pass" -alias "$alias_name" > /dev/null 2>&1; then
    echo "Alias $alias_name already exists in keystore, deleting it first..."
    keytool -delete -cacerts -storepass "$keystore_pass" -alias "$alias_name"
  fi

  # Import CA
  keytool -importcert \
    -alias "$alias_name" \
    -cacerts \
    -storepass "$keystore_pass" \
    -file "$ca_file" \
    -noprompt

  echo "SLM root CA successfully imported to Java keystore with alias $alias_name"
}

wait_for_consul() {
  while true; do
    local consul_leader
    consul_leader=$(curl -s "${CONSUL_URL}/v1/status/leader" || true)
    if [ -n "$consul_leader" ]; then
      break
    fi
    echo "[ Waiting for Consul at ${CONSUL_URL} ... ]"
    sleep 1
  done
}

wait_for_resource_management() {
  # Wait until Resource Management is running
  until curl -m 5 -s -k --location --request GET "$RESOURCEMANAGEMENT_URL/v3/api-docs" > /dev/null; do
    echo "Resource Management is unavailable -> sleeping"
    sleep 1
  done
}

wait_for_service_management() {
  # Wait until Service Management is running
  until curl -m 5 -s -k --location --request GET "$SERVICEMANAGEMENT_URL/v3/api-docs" > /dev/null; do
    echo "Service Management is unavailable -> sleeping"
    sleep 1
  done
}

wait_for_keycloak() {
  # Wait until Keycloak is running
  until curl -m 5 -s -k --location --request GET "$KEYCLOAK_AUTHSERVERURL/realms/$KEYCLOAK_REALM/.well-known/openid-configuration" > /dev/null; do
    echo "Keycloak is unavailable -> sleeping"
    sleep 1
  done
}

start_app() {
  # Start App
  echo "[ Starting Java Application... ]"
  java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
}

###endregion Methods