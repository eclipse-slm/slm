FROM ghcr.io/eclipse-slm/awx/awx-fixed-files:15.0.1

ENV AWX_SKIP_MIGRATIONS="0" \
    AWX_ADMIN_USER="admin" \
    AWX_ADMIN_PASSWORD="password" \
    DATABASE_NAME="awx" \
    DATABASE_USER="awx" \
    DATABASE_PASSWORD="awxpass" \
    DATABASE_HOST="awx-postgres" \
    DATABASE_PORT=5432 \
    http_proxy="" \
    https_proxy="" \
    no_proxy="" \
    SUPERVISOR_WEB_CONFIG_PATH='/etc/supervisord.conf'

USER root

CMD /usr/bin/launch_awx_task.sh
