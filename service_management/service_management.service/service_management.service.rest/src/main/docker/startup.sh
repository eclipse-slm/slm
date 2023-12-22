#! /bin/bash

# Wait until Vault configuration is present
VAULT_CONFIG_DIRECTORY="/app/vault/"
until [ -f "$VAULT_CONFIG_DIRECTORY/role_id" ]; do
  echo "Vault role_id config file '$VAULT_CONFIG_DIRECTORY/role_id' missing -> sleeping"
  sleep 3
done
export VAULT_APPROLE_ROLEID=$(cat "$VAULT_CONFIG_DIRECTORY/role_id")

until [ -f "$VAULT_CONFIG_DIRECTORY/secret_id" ]; do
  echo "Vault secret_id config file '$VAULT_CONFIG_DIRECTORY/secret_id' missing -> sleeping"
  sleep 3
done
export VAULT_APPROLE_SECRETID=$(cat "$VAULT_CONFIG_DIRECTORY/secret_id")

# Wait until Keycloak configuration is present
KEYCLOAK_CONFIG_DIRECTORY="/app/keycloak/"
while [ -z "$(ls -A $KEYCLOAK_CONFIG_DIRECTORY)" ]; do
  echo "Keycloak config file(s) in $KEYCLOAK_CONFIG_DIRECTORY missing -> sleeping"
  sleep 3
done

# Wait until Keycloak is running
until curl -m 5 -s --location --request GET "$KEYCLOAK_AUTHSERVERURL/realms/fabos/.well-known/openid-configuration"; do
  echo "Keycloak is unavailable -> sleeping"
  sleep 1
done

# Wait until Consul configuration is present
CONSUL_TOKEN_FILE="/app/consul/consul_token"
until [ -f "$CONSUL_TOKEN_FILE" ]; do
  echo "Consul token config file '$CONSUL_TOKEN_FILE' missing -> sleeping"
  sleep 3
done
export CONSUL_ACLTOKEN=$(cat "$CONSUL_TOKEN_FILE")

# Wait until AWX configuration is present
AWX_CONFIG_DIRECTORY="/app/awx/"
while [ -z "$(ls -A $AWX_CONFIG_DIRECTORY)" ]; do
  echo "AWX config file(s) in $AWX_CONFIG_DIRECTORY missing -> sleeping"
  sleep 3
done
export AWX_USERNAME=$(cat "$AWX_CONFIG_DIRECTORY/awx_username")
export AWX_PASSWORD=$(cat "$AWX_CONFIG_DIRECTORY/awx_password")

# Wait until BaSyx AAS Registry is running
until curl -m 5 -s --location --request GET "${BASYX_AASREGISTRY_PATH}/health" > /dev/null; do
  echo "AAS Registry is unavailable -> sleeping"
  sleep 1
done

# Wait until BaSyx AAS Server is running
until curl -m 5 -s --location --request GET "${BASYX_AASSERVER_PATH}/health" > /dev/null; do
  echo "AAS Server is unavailable -> sleeping"
  sleep 1
done

# Wait until Minio configuration is present
MINIO_CONFIG_DIRECTORY="/app/minio/"
while [ -z "$(ls -A $MINIO_CONFIG_DIRECTORY)" ]; do
  echo "MINIO config file(s) in $MINIO_CONFIG_DIRECTORY missing -> sleeping"
  sleep 3
done
export MINIO_ACCESS_KEY="service-management"
export MINIO_SECRET_KEY=$(cat "$AWX_CONFIG_DIRECTORY/service_management_secret")

# Get database configuration
DATABASE_CONFIGDIR="/app/database/config"
export DATABASE_USERNAME=$(cat "$DATABASE_CONFIGDIR/user")
export DATABASE_PASSWORD=$(cat "$DATABASE_CONFIGDIR/password")
export DATABASE_SCHEMA=$(cat "$DATABASE_CONFIGDIR/schema")

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
