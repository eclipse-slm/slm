FROM ghcr.io/eclipse-slm/ansible:2.11.9

ENV VAULT_URL=http://vault:8200 \
    VAULT_CONFIG_VIA=SHARED_VOLUME \
#    VAULT_CONFIG_VIA=ENVIRONMENT_VARIABLES \
#    VAULT_TOKEN=s.GN32jmokCi9S7O1qM3P5pMLv \
    KEYCLOAK_AUTHSERVERURL=http://keycloak:8080/auth \
    KEYCLOAK_REALM=fabos

COPY src /src

WORKDIR src

CMD ansible-playbook main.yml -vv
