#! /bin/bash

# Wait until Resource Management is running
until curl -m 5 -s -k --location --request GET "$RESOURCEMANAGEMENT_URL/v3/api-docs" > /dev/null; do
  echo "Resource Management is unavailable -> sleeping"
  sleep 1
done

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
