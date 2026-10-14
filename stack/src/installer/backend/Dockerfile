FROM ghcr.io/eclipse-slm/ansible:2.17.2

ENV INSTALLER_WORKING_DIR=/ansible/installer/src \
	INSTALLER_PLAYBOOK=install.yml \
	INSTALLER_UNINSTALL_WORKING_DIR=/ansible/installer/src \
	INSTALLER_UNINSTALL_PLAYBOOK=uninstall.yml \
	INSTALLER_ENV_FILE=/env/env.yml \
	INSTALLER_RUNNER_MODE=subprocess \
	INSTALL_DIRECTORY=/opt/eclipse-slm

# MinIO archived the open-source mc project and dl.min.io now returns 410 for
# every download path, so pin the last open-source release from GitHub instead.
ARG MC_VERSION=RELEASE.2025-08-13T08-35-41Z
ARG MC_SHA256=01f866e9c5f9b87c2b09116fa5d7c06695b106242d829a8bb32990c00312e891
RUN curl -fL "https://github.com/minio/mc/releases/download/${MC_VERSION}/mc.linux-amd64.${MC_VERSION}" --create-dirs -o /usr/local/bin/mc
RUN echo "${MC_SHA256}  /usr/local/bin/mc" | sha256sum -c -
RUN chmod +x /usr/local/bin/mc
RUN /usr/local/bin/mc --version

WORKDIR /app/backend

COPY backend /app/backend
RUN pip install --no-cache-dir -e .

WORKDIR /app
COPY backend/entrypoint-api.sh /app/entrypoint-api.sh
RUN chmod +x /app/entrypoint-api.sh

COPY ansible /ansible/installer/src
RUN mkdir -p /env && touch /env/env.override.yml

RUN ansible-galaxy install -r /ansible/installer/src/requirements.yml

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint-api.sh"]
