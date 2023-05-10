FROM openjdk:18.0.2.1-jdk-slim-buster

ENV KEYCLOAK_CONFIG_TENANTS_CONFIGPATH="/app/keycloak" \
    DATABASE_HOST="notification-service-database" \
    DATABASE_PORT=3306 \
    AWX_SCHEME="http" \
    AWX_HOST="awxweb" \
    AWX_PORT="8052" \
    AWX_USER="admin" \
    AWX_PASSWORD="password"

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

VOLUME "/app/keycloak"

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
