#! /bin/bash

echo "Init Directory: $SERVICEMANAGEMENT_INITDIRECTORIES"

# Wait until Service Management is running
until curl -m 5 -s --location --request GET "$SERVICEMANAGEMENT_URL/v3/api-docs" > /dev/null; do
  echo "Service Management is unavailable -> sleeping"
  sleep 1
done

if [ "$KEYCLOAK_CONFIG_VIA" == "SHARED_VOLUME" ]; then
    echo "Using Keycloak config via shared volume"
    # Wait until Keycloak configuration is present
    KEYCLOAK_CONFIG_DIRECTORY="/app/keycloak"
    while [ -z "$(ls -A $KEYCLOAK_CONFIG_DIRECTORY)" ]; do
      echo "Keycloak config file(s) in $KEYCLOAK_CONFIG_DIRECTORY missing -> sleeping"
      sleep 3
    done

    export KEYCLOAK_AUTHSERVERURL=$(cat "$KEYCLOAK_CONFIG_DIRECTORY/auth-server-url")
    export KEYCLOAK_REALM=$(cat "$KEYCLOAK_CONFIG_DIRECTORY/realm")
    export KEYCLOAK_USERNAME=$(cat "$KEYCLOAK_CONFIG_DIRECTORY/username")
    export KEYCLOAK_PASSWORD=$(cat "$KEYCLOAK_CONFIG_DIRECTORY/password")
elif [ "$KEYCLOAK_CONFIG_VIA" == "ENVIRONMENT_VARIABLES" ]; then
    echo "Using Keycloak config via environment variables"
else
  echo "Unknown Keycloak config type '$KEYCLOAK_CONFIG_VIA' --> Exiting"
  exit
fi

# Wait until Keycloak is running
until curl -m 5 -s --location --request GET "$KEYCLOAK_AUTHSERVERURL/realms/$KEYCLOAK_REALM/.well-known/openid-configuration" > /dev/null; do
  echo "Keycloak is unavailable -> sleeping"
  sleep 1
done

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
