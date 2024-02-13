FROM quay.io/keycloak/keycloak:19.0.3

ENV KC_DB_HOST=keycloak-database \
    KC_DB_URL_HOST=keycloak-database \
    KC_DB_SCHEMA=keycloak \
    KC_DB_USERNAME=keycloak \
    KC_DB_PASSWORD=password \
    KEYCLOAK_ADMIN=admin \
    KEYCLOAK_ADMIN_PASSWORD=password \
    KC_HTTP_RELATIVE_PATH=/auth

USER 0

COPY themes/fabos /opt/keycloak/themes/fabos

USER 1000

CMD ["start-dev", "--db mariadb", "--transaction-xa-enabled=false", "--health-enabled=true"]
