FROM ghcr.io/eclipse-slm/ansible:2.11.9

ENV CONSUL_URL=http://consul:8500 \
    CONSUL_CONFIG_VIA=SHARED_VOLUME \
#    CONSUL_CONFIG_VIA=ENVIRONMENT_VARIABLES \
#    CONSUL_TOKEN=MyConsulToken \
    KEYCLOAK_AUTHSERVERURL=http://keycloak:8080/auth

COPY src /src

WORKDIR src

CMD ansible-playbook main.yml
