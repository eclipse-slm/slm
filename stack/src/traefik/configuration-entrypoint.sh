#!/bin/sh

echo "[ Configuring Vault Agent... ]"
if [ -n "$VAULT_APP_ROLE_ROLE_ID" ]; then
  echo "$VAULT_APP_ROLE_ROLE_ID" > /vault/role_id
else
  echo "ERROR: Environment variable '$VAULT_APP_ROLE_ROLE_ID' not set or empty!"
  exit 1
fi

if [ -n "$VAULT_APP_ROLE_SECRET_ID" ]; then
  echo "$VAULT_APP_ROLE_SECRET_ID" > /vault/secret_id
else
  echo "ERROR: Environment variable 'VAULT_APP_ROLE_SECRET_ID' not set or empty!"
  exit 1
fi

echo "[ Starting Vault Agent... ]"
vault agent -config=/vault/vault_agent.hcl &
sleep 5 # Give Vault Agent some time to start and get certificates

echo "[ Starting Traefik... ]"
# Forwarding to the base entrypoint
exec traefik "$@"