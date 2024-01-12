FROM ghcr.io/eclipse-slm/ansible:2.11.9

ENV CONSUL_URL=http://consul:8500 \
    CONSUL_TOKEN=bOKGjerhQcCsjphbg3Wqg40r \
    VAULT_URL=http://vault:8200

RUN pip install docker docker-compose python-consul
RUN mkdir /compose

COPY src /src
COPY env.yml /env/env.yml

WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml -v
