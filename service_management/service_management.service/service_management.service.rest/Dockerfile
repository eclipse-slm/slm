FROM openjdk:18.0.2.1-jdk-slim-buster
MAINTAINER Matthias Schneider (matthias.schneider@ipa.fraunhofer.de)

ENV KEYCLOAK_CONFIG_TENANTS_CONFIGPATH="/app/keycloak" \
    DATABASE_HOST="service-management-database" \
    DATABASE_PORT=3306 \
    RESOURCEMANAGEMENT_URL="http://resource-management:9010" \
    NOTIFICATIONSERVICE_SCHEME="http" \
    NOTIFICATIONSERVICE_HOST="notification-service"  \
    NOTIFICATIONSERVICE_PORT=9001 \
    CONSUL_SCHEME="http" \
    CONSUL_HOST="consul" \
    CONSUL_PORT=8500 \
    CONSUL_DATACENTER="fabos" \
    CONSUL_ACLTOKEN="password" \
    AWX_SCHEME="http" \
    AWX_HOST="awxweb" \
    AWX_PORT="8052" \
    AWX_USER="admin" \
    AWX_PASSWORD="password" \
    VAULT_SCHEME="http" \
    VAULT_HOST="vault" \
    VAULT_PORT=8200 \
    VAULT_AUTHENTICATION="APPROLE" \
    BASYX_AASREGISTRY_PATH="http://aas-registry:4000" \
    BASYX_AASSERVER_PATH="http://aas-server:4001"

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

VOLUME "/app/keycloak"

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
