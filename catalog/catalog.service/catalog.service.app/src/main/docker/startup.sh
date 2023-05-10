#! /bin/bash

# Get database configuration
DATABASE_CONFIGDIR="/app/database/config"
export DATABASE_USERNAME=$(cat "$DATABASE_CONFIGDIR/user")
export DATABASE_PASSWORD=$(cat "$DATABASE_CONFIGDIR/password")
export DATABASE_SCHEMA=$(cat "$DATABASE_CONFIGDIR/schema")

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
