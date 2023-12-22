#!/bin/bash

URL="http://minio:9090/"
TIMEOUT=30
SLEEP_INTERVAL=5

until [[ "$(curl -s -o /dev/null -w '%{http_code}' "$URL")" == "200" ]]; do
    echo "Waiting for $URL..."
    sleep "$SLEEP_INTERVAL"
done

ALIAS="slm"

service_access_key=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 20)
service_password=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 32)

awx_access_key=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 20)
awx_password=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 32)

./mc alias set $ALIAS http://minio:9000/ admin password

USER="service-management"
./mc admin user add $ALIAS $USER $service_password
./mc admin policy attach $ALIAS readwrite --user $USER
./mc admin user svcacct add --access-key $service_access_key --secret-key $service_password $ALIAS $USER &> /dev/null
echo Access Key for $USER created


USER="awx"
./mc admin user add $ALIAS $USER $service_password
./mc admin policy attach $ALIAS readwrite --user $USER
./mc admin user svcacct add --access-key $awx_access_key --secret-key $awx_password $ALIAS $USER &> /dev/null
echo Access Key for $USER created


echo $service_access_key > /app/service-management/service_management_access
echo $service_password > /app/service-management/service_management_secret

echo $awx_access_key > /app/awx/awx_access
echo $awx_password > /app/awx/awx_secret