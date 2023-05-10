#! /bin/bash

KEYCLOAK_DATABASE_CONFIGDIR="/keycloak/database/config"

# Wait until Keycloak configuration is present
while [ -z "$(ls -A $KEYCLOAK_DATABASE_CONFIGDIR)" ]; do
  echo "Keycloak database config file(s) in $KEYCLOAK_DATABASE_CONFIGDIR missing -> sleeping"
  sleep 3
done

export KC_DB_PASSWORD=$(cat "$KEYCLOAK_DATABASE_CONFIGDIR/password")

bash /opt/keycloak/bin/kc.sh start-dev --db mariadb --transaction-xa-enabled=false
