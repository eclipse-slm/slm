FROM ghcr.io/eclipse-slm/ansible:2.11.9

ENV KEYCLOAK_AUTHSERVERURL=http://keycloak:8080/auth \
    KEYCLOAK_ADMIN_USER=admin \
    KEYCLOAK_ADMIN_PASSWORD=password \
    KEYCLOAK_DEFAULT_USER_PASSWORD=password

COPY src /src

WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml
