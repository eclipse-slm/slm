FROM ghcr.io/eclipse-slm/ansible:2.17.2

ENV INSTALLER_WORKING_DIR=/ansible/installer/src \
	INSTALLER_PLAYBOOK=main.yml \
	INSTALLER_UNINSTALL_WORKING_DIR=/ansible/uninstaller/src \
	INSTALLER_UNINSTALL_PLAYBOOK=main.yml \
	INSTALLER_ENV_FILE=/env/env.yml \
	INSTALLER_RUNNER_MODE=subprocess \
	INSTALL_DIRECTORY=/opt/eclipse-slm \
	DEPLOYMENT_HOST_HOSTNAME=localhost

RUN curl -fL https://dl.min.io/client/mc/release/linux-amd64/mc \
      --create-dirs \
      -o /usr/local/bin/mc
RUN chmod +x /usr/local/bin/mc
RUN /usr/local/bin/mc --version

WORKDIR /app/backend

COPY backend /app/backend
RUN pip install --no-cache-dir -e .

WORKDIR /app
COPY backend/entrypoint-api.sh /app/entrypoint-api.sh
RUN chmod +x /app/entrypoint-api.sh

COPY ansible/installer/src /ansible/installer/src
COPY ansible/uninstaller/src /ansible/uninstaller/src
COPY ansible/installer/env.yml /env/env.yml
COPY ansible/installer/env.override.yml /env/env.override.yml

RUN ansible-galaxy install -r /ansible/installer/src/requirements.yml
RUN ansible-galaxy install -r /ansible/uninstaller/src/requirements.yml

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint-api.sh"]
