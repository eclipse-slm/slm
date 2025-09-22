FROM openjdk:19-jdk-slim-bullseye
MAINTAINER Matthias Schneider (matthias.schneider@ipa.fraunhofer.de)

ENV KEYCLOAK_CONFIG_TENANTS_CONFIGPATH="/app/keycloak" \
    SPRING_CLOUD_CONSUL_ENABLED=true \
    CONSUL_SCHEME="http" \
    CONSUL_HOST="consul" \
    CONSUL_PORT=8500 \
    CONSUL_DATACENTER="fabos" \
    CONSUL_ACLTOKEN="password" \
    DATABASE_HOST="catalog-database" \
    DATABASE_PORT=3306

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

VOLUME "/app/keycloak"

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
