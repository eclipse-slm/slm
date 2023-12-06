#!/bin/sh
JSON_STRING='window.configs = { \
  "VUE_APP_RESOURCE_REGISTRY_URL":"'"${RESOURCE_REGISTRY_URL}"'", \
  "VUE_APP_SERVICE_REGISTRY_URL":"'"${SERVICE_REGISTRY_URL}"'", \
  "VUE_APP_NOTIFICATION_SERVICE_URL":"'"${NOTIFICATION_SERVICE_URL}"'", \
  "VUE_APP_KEYCLOAK_URL":"'"${KEYCLOAK_URL}"'", \
  "VUE_APP_KEYCLOAK_REALM":"'"${KEYCLOAK_REALM}"'", \
  "VUE_APP_KEYCLOAK_CLIENT_ID":"'"${KEYCLOAK_CLIENT_ID}"'", \
  "VUE_APP_AWX_URL":"'"${AWX_URL}"'", \
  "VUE_APP_CATALOG_SERVICE_URL":"'"${CATALOG_SERVICE_URL}"'", \
  "VUE_APP_BASYX_AAS_GUI_URL":"'"${BASYX_AAS_GUI_URL}"'", \
}'
sed -i "s@// CONFIGURATIONS_PLACEHOLDER@${JSON_STRING}@" /usr/share/nginx/html/index.html

#envsubst '$RESOURCE_REGISTRY_URL,$SERVICE_REGISTRY_URL,$NOTIFICATION_SERVICE_URL,$CATALOG_SERVICE_URL' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# Wait until Keycloak is running
echo "Keycloak availability test URL: $KEYCLOAK_URL/realms/$KEYCLOAK_REALM/.well-known/openid-configuration"
until curl -m 5 -s --location --request GET "$KEYCLOAK_URL/realms/$KEYCLOAK_REALM/.well-known/openid-configuration" > /dev/null; do
  echo "Keycloak is unavailable -> sleeping"
  sleep 1
done

# Wait until Notification Service is running
until curl -m 5 -s --location --request GET "$NOTIFICATION_SERVICE_URL/v3/api-docs" > /dev/null; do
  echo "Notification Service is unavailable -> sleeping"
  sleep 1
done

# Wait until Resource Management is running
until curl -m 5 -s --location --request GET "$RESOURCE_REGISTRY_URL/v3/api-docs" > /dev/null; do
  echo "Resource Management is unavailable -> sleeping"
  sleep 1
done

# Wait until Service Management is running
until curl -m 5 -s --location --request GET "$SERVICE_REGISTRY_URL/v3/api-docs" > /dev/null; do
  echo "Service Management is unavailable -> sleeping"
  sleep 1
done

# Wait until Consul configuration is present
#CONSUL_TOKEN_FILE="/app/consul/consul_token"
#until [ -f "$CONSUL_TOKEN_FILE" ]; do
#  echo "Consul token config file '$CONSUL_TOKEN_FILE' missing -> sleeping"
#  sleep 3
#done
#export CONSUL_TOKEN=$(cat "$CONSUL_TOKEN_FILE")

exec "$@"
