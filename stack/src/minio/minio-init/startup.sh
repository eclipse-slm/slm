#!/bin/bash

URL="http://minio:9090/"
TIMEOUT=30
SLEEP_INTERVAL=5

until [[ "$(curl -s -o /dev/null -w '%{http_code}' "$URL")" == "200" ]]; do
    echo "Waiting for $URL..."
    sleep "$SLEEP_INTERVAL"
done

ALIAS="slm"

./mc alias set $ALIAS http://minio:9000/ admin password

service_access_key_file=/app/service-management/service_management_access
service_password_file=/app/service-management/service_management_secret
if ! test -f "$service_access_key_file" && ! test -f "$service_password_file"; then
  service_access_key=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 20)
  service_password=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 32)

  USER="service-management"
  ./mc admin user add $ALIAS $USER $service_password
  ./mc admin policy attach $ALIAS readwrite --user $USER
  ./mc admin user svcacct add --access-key $service_access_key --secret-key $service_password $ALIAS $USER &> /dev/null
  echo Access Key for $USER created

  echo $service_access_key > $service_access_key_file
  echo $service_password > $service_password_file
fi

awx_access_key_file=/app/awx/awx_access
awx_password_file=/app/awx/awx_secret
if ! test -f "$awx_access_key_file" && ! test -f "$awx_password_file"; then
  awx_access_key=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 20)
  awx_password=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 32)

  USER="awx"
  ./mc admin user add $ALIAS $USER $service_password
  ./mc admin policy attach $ALIAS readwrite --user $USER
  ./mc admin user svcacct add --access-key $awx_access_key --secret-key $awx_password $ALIAS $USER &> /dev/null
  echo Access Key for $USER created

  echo $awx_access_key > $awx_access_key_file
  echo $awx_password > $awx_password_file
fi