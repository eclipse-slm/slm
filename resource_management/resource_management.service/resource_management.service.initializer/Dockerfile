FROM eclipse-temurin:21-jdk-noble

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
# common.sh is copied from common/common.parent/common.parent.service/src/docker/common.sh to target/copied-sources via maven resources plugin
COPY target/copied-sources/common.sh /app/common.sh
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

COPY src/main/resources/init/capabilities.json /init/capabilities.json
COPY src/main/resources/init/profiler.json /init/profiler.json

WORKDIR /app

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
