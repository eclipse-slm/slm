#! /bin/bash

# Wait until Keycloak configuration is present
KEYCLOAK_CONFIG_DIRECTORY="/app/keycloak/"
while [ -z "$(ls -A $KEYCLOAK_CONFIG_DIRECTORY)" ]; do
  echo "Keycloak config file(s) in $KEYCLOAK_CONFIG_DIRECTORY missing - sleeping"
  sleep 3
done

# Wait until Keycloak is running
until curl -m 5 -s --location --request GET "$KEYCLOAK_AUTHSERVERURL/auth/realms/fabos/.well-known/openid-configuration"; do
  echo "Keycloak is unavailable - sleeping"
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

# Get database configuration
DATABASE_CONFIGDIR="/app/database/config"
export DATABASE_USERNAME=$(cat "$DATABASE_CONFIGDIR/user")
export DATABASE_PASSWORD=$(cat "$DATABASE_CONFIGDIR/password")
export DATABASE_SCHEMA=$(cat "$DATABASE_CONFIGDIR/schema")

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
