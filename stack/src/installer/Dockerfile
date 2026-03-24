FROM ghcr.io/eclipse-slm/ansible:2.17.2

ENV CONSUL_URL=http://consul:8500 \
    CONSUL_TOKEN=bOKGjerhQcCsjphbg3Wqg40r \
    VAULT_URL=http://vault:8200 \
    DEPLOYMENT_HOST_HOSTNAME=localhost \
    ANSIBLE_CMD_PARAMETERS="-v"

RUN mkdir /compose

RUN curl https://dl.min.io/client/mc/release/linux-amd64/mc \
      --create-dirs \
      -o /usr/local/bin/mc

RUN chmod +x /usr/local/bin/mc

COPY src /src
COPY env.yml /env/env.yml
COPY env.override.yml /env/env.override.yml

WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml $ANSIBLE_CMD_PARAMETERS
