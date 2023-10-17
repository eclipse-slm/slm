#! /bin/bash

# Wait until Consul configuration is present
CONSUL_TOKEN_FILE="/app/consul/consul_token"
until [ -f "$CONSUL_TOKEN_FILE" ]; do
  echo "Consul token config file '$CONSUL_TOKEN_FILE' missing -> sleeping"
  sleep 3
done
export CONSUL_ACLTOKEN=$(cat "$CONSUL_TOKEN_FILE")

# Get database configuration
DATABASE_CONFIGDIR="/app/database/config"
export DATABASE_USERNAME=$(cat "$DATABASE_CONFIGDIR/user")
export DATABASE_PASSWORD=$(cat "$DATABASE_CONFIGDIR/password")
export DATABASE_SCHEMA=$(cat "$DATABASE_CONFIGDIR/schema")

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
