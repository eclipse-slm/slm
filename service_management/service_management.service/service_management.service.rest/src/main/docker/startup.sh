#! /bin/bash

echo $CONSUL_SCHEME
echo $CONSUL_HOST
echo $CONSUL_PORT
echo $CONSUL_ACLTOKEN


# Wait until BaSyx AAS Registry is running
until curl -m 5 -s --location --request GET "${BASYX_AASREGISTRY_PATH}/health" > /dev/null; do
  echo "AAS Registry is unavailable -> sleeping"
  sleep 1
done

# Wait until BaSyx AAS Server is running
until curl -m 5 -s --location --request GET "${BASYX_AASSERVER_PATH}/health" > /dev/null; do
  echo "AAS Server is unavailable -> sleeping"
  sleep 1
done

# Start App
java -jar -Djava.security.egd=file:/dev/./urandom /app/app.jar
