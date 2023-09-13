#! /bin/bash

# Wait until Resource Management is running
until curl -m 5 -s --location --request GET "$RESOURCEMANAGEMENT_URL/v3/api-docs" > /dev/null; do
  echo "Resource Management is unavailable -> sleeping"
  sleep 1
done

# Wait until Consul configuration is present
CONSUL_TOKEN_FILE="/app/consul/consul_token"
until [ -f "$CONSUL_TOKEN_FILE" ]; do
  echo "Consul token config file '$CONSUL_TOKEN_FILE' missing -> sleeping"
  sleep 3
done
export CONSUL_ACLTOKEN=$(cat "$CONSUL_TOKEN_FILE")

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
