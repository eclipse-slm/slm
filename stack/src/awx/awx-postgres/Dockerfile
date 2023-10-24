FROM postgres:12

ENV POSTGRES_USER=awx \
    POSTGRES_PASSWORD=awxpass \
    POSTGRES_DB=awx \
    http_proxy="" \
    https_proxy="" \
    no_proxy=""

COPY awx.sql /docker-entrypoint-initdb.d
