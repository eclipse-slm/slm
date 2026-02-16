# Dockerfile is copied to target/copied-sources via maven resources plugin (see module pom.xml of using module)

FROM eclipse-temurin:21-jdk-noble
MAINTAINER Matthias Schneider (matthias.schneider@ipa.fraunhofer.de)

ENV CONSUL_SCHEME="http" \
    CONSUL_HOST="consul" \
    CONSUL_PORT=8500 \
    CONSUL_DATACENTER="fabos" \
    CONSUL_ACLTOKEN="password" \
    AAS_BROKER="aas-broker"

RUN apt update && \
    apt install -y curl jq

COPY target/*-exec.jar /app/app.jar
# common.sh and startup.sh are copied to target/copied-sources via maven resources plugin (see module pom.xml of using module)
COPY target/copied-sources/common.sh /app/common.sh
COPY target/copied-sources/startup.sh /app/startup.sh
RUN chmod +x /app/startup.sh

WORKDIR /app

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]
