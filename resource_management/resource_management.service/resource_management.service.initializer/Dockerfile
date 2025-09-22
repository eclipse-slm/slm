FROM openjdk:19-jdk-slim-bullseye

ENV RESOURCEMANAGEMENT_URL="http://resource-management:9010" \
    RESOURCEMANAGEMENT_INITDIRECTORIES="/init/" \
    CONSUL_SCHEME="http" \
    CONSUL_HOST="consul" \
    CONSUL_PORT=8500 \
    CONSUL_DATACENTER="fabos" \
    CONSUL_ACLTOKEN="password" \
    KEYCLOAK_AUTHSERVERURL="http://localhost:7080/auth" \
    KEYCLOAK_REALM="fabos" \
    KEYCLOAK_USERNAME="fabos" \
    KEYCLOAK_PASSWORD="password"

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/resources/init/capabilities.json /init/capabilities.json
COPY src/main/resources/init/profiler.json /init/profiler.json
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
