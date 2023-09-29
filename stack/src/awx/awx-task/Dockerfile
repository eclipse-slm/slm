ENV AWX_SKIP_MIGRATIONS="0" \
    AWX_ADMIN_USER="admin" \
    AWX_ADMIN_PASSWORD="password" \
    DATABASE_NAME="awx" \
    DATABASE_USER="awx" \
    DATABASE_PASSWORD="awxpass" \
    DATABASE_HOST="postgres" \
    DATABASE_PORT=5432 \
    http_proxy="" \
    https_proxy="" \
    no_proxy="" \
    SUPERVISOR_WEB_CONFIG_PATH='/etc/supervisord.conf' \
    AWX_TAG=22.7.0

FROM ghcr.io/eclipse-slm/awx/base:${AWX_TAG}


COPY conf/ /etc/tower/conf.d/
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/nginx.locations.conf /etc/nginx/conf.d/nginx.locations.conf
COPY SECRET_KEY /etc/tower/SECRET_KEY
COPY receptor/ /etc/receptor/
COPY supervisor.conf /etc/supervisord.conf


USER root

WORKDIR /tmp/

RUN git clone https://github.com/ansible/awx.git && \
    cd awx && \
    git checkout tags/${AWX_TAG}

RUN cp -rf /tmp/awx /awx_devel/

COPY ./admin_password.yml /awx_devel/tools/docker-compose/_sources/secrets

WORKDIR /awx_devel

RUN echo "GENERATE_SOURCEMAP=false" >> .env

RUN make clean-ui ui-devel


RUN cd awx/ui && npx update-browserslist-db@latest

CMD "/awx_devel/launch_awx.sh"


