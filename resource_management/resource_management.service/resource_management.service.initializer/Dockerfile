FROM openjdk:18.0.2.1-jdk-slim-buster

ENV RESOURCEREGISTRY_URL="http://resource-management:9010" \
    RESOURCEREGISTRY_INITDIRECTORIES="/init/" \
    KEYCLOAK_AUTHSERVERURL="http://localhost:7080/auth" \
    KEYCLOAK_REALM="fabos" \
    KEYCLOAK_USERNAME="fabos" \
    KEYCLOAK_PASSWORD="password"

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/resources/init/capabilities.json /init/capabilities.json
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
