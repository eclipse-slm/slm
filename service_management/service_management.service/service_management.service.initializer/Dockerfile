FROM openjdk:19-jdk-slim-bullseye
MAINTAINER Matthias Schneider (matthias.schneider@ipa.fraunhofer.de)

ENV SERVICEMANAGEMENT_URL="http://service-management:9020"\
    SERVICEMANAGEMENT_INITDIRECTORIES=""\
    SERVICEMANAGEMENT_GITREPOS_CLONEDIRECTORY="/tmp"\
    SERVICEMANAGEMENT_GITREPOS_URLS="https://github.com/FabOS-AI/fabos-slm-service-registry-content.git"\
    KEYCLOAK_AUTHSERVERURL="http://keycloak:7080/auth" \
    KEYCLOAK_REALM="fabos" \
    KEYCLOAK_USERNAME="fabos" \
    KEYCLOAK_PASSWORD="password"

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
COPY src/main/docker/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app
RUN mkdir -p /app/custom-repo

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
