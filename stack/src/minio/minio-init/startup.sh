#!/bin/bash

service_password := date +%s | sha256sum | base64 | head -c 32
awx_password := date +%s | sha256sum | base64 | head -c 32

./mc alias set slm http://minio:9000/ admin password

./mc admin user add slm service-management $service_password
./mc admin user svcacct add --access-key service-management --secret-key $service_password slm service-management

./mc admin user add slm awx $awx_password
./mc admin user svcacct add --access-key awx --secret-key $awx_password slm awx

echo $service_password >> /app/service-management/service_management_secret
echo $awx_password >> /app/awx/awx_secret