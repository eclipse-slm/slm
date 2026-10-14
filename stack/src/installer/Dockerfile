FROM ghcr.io/eclipse-slm/ansible:2.17.2

ENV CONSUL_URL=http://consul:8500 \
    CONSUL_TOKEN=bOKGjerhQcCsjphbg3Wqg40r \
    VAULT_URL=http://vault:8200 \
    DEPLOYMENT_HOST_HOSTNAME=localhost \
    ANSIBLE_CMD_PARAMETERS="-v"

# MinIO archived the open-source mc project and dl.min.io now returns 410 for
# every download path, so pin the last open-source release from GitHub instead.
ARG MC_VERSION=RELEASE.2025-08-13T08-35-41Z
ARG MC_SHA256=01f866e9c5f9b87c2b09116fa5d7c06695b106242d829a8bb32990c00312e891
RUN curl -fL "https://github.com/minio/mc/releases/download/${MC_VERSION}/mc.linux-amd64.${MC_VERSION}" --create-dirs -o /usr/local/bin/mc
RUN echo "${MC_SHA256}  /usr/local/bin/mc" | sha256sum -c -
RUN chmod +x /usr/local/bin/mc
RUN /usr/local/bin/mc --version

COPY src /src
RUN mkdir -p /env && touch /env/env.override.yml

WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml $ANSIBLE_CMD_PARAMETERS
