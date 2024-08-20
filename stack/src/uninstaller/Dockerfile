FROM ghcr.io/eclipse-slm/ansible:2.17.2

ENV CONSUL_URL=http://consul:8500 \
    CONSUL_TOKEN=bOKGjerhQcCsjphbg3Wqg40r \
    VAULT_URL=http://vault:8200 \
    ANSIBLE_CMD_PARAMETERS="-v"

COPY src /src
WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml $ANSIBLE_CMD_PARAMETERS
