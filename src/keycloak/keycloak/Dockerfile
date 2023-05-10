FROM quay.io/keycloak/keycloak:19.0.2

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
#COPY themes/base/login/login.ftl /opt/jboss/keycloak/themes/base/login/login.ftl

COPY startup.sh /keycloak/startup.sh
RUN chmod +x /keycloak/startup.sh

USER 1000

ENTRYPOINT [ "/keycloak/startup.sh" ]

CMD [ "/keycloak/startup.sh" ]
