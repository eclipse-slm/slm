#! /bin/bash

echo "Init Directory: $SERVICEMANAGEMENT_INITDIRECTORIES"

# Wait until Service Management is running
until curl -m 5 -s -k --location --request GET "$SERVICEMANAGEMENT_URL/v3/api-docs" > /dev/null; do
  echo "Service Management is unavailable -> sleeping"
  sleep 1
done

# Wait until Keycloak is running
until curl -m 5 -s -k --location --request GET "$KEYCLOAK_AUTHSERVERURL/realms/$KEYCLOAK_REALM/.well-known/openid-configuration" > /dev/null; do
  echo "Keycloak is unavailable -> sleeping"
  sleep 1
done

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
