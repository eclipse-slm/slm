#! /bin/bash

set -m

MYSQL_DATABASE_CONFIGDIR="/config"
MYSQL_ROOT_PASSWORD_FILE="$MYSQL_DATABASE_CONFIGDIR/root_password"
MYSQL_USER_FILE="$MYSQL_DATABASE_CONFIGDIR/user"
MYSQL_PASSWORD_FILE="$MYSQL_DATABASE_CONFIGDIR/password"
MYSQL_SCHEMA_FILE="$MYSQL_DATABASE_CONFIGDIR/schema"

if [ -f "$MYSQL_ROOT_PASSWORD_FILE" ]; then
  echo "Passwords already generated"
  export MYSQL_ROOT_PASSWORD=$(cat "$MYSQL_ROOT_PASSWORD_FILE")
  export MYSQL_PASSWORD=$(cat "$MYSQL_PASSWORD_FILE")
else
  mkdir -p "$MYSQL_DATABASE_CONFIGDIR"

  export MYSQL_ROOT_PASSWORD=$(pwgen -s -1 24)
  echo "$MYSQL_ROOT_PASSWORD" > $MYSQL_ROOT_PASSWORD_FILE

  export MYSQL_PASSWORD=$(pwgen -s -1 24)
  echo "$MYSQL_PASSWORD" > $MYSQL_PASSWORD_FILE

  echo "$MYSQL_USER" > $MYSQL_USER_FILE
  echo "$MYSQL_DATABASE" > $MYSQL_SCHEMA_FILE

  echo "Config generated: $MYSQL_DATABASE_CONFIGDIR"
fi

echo "----------------------------------------"
echo "Root Password: Auto-generated (see $MYSQL_ROOT_PASSWORD_FILE)"
echo "Username: $MYSQL_USER"
echo "Password: Auto-generated (see $MYSQL_PASSWORD_FILE)"
echo "Schema: $MYSQL_DATABASE"
echo "----------------------------------------"

bash docker-entrypoint.sh mysqld

fg %1
