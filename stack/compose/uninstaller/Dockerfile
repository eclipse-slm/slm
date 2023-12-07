FROM ghcr.io/eclipse-slm/ansible:2.11.9

ENV CONSUL_URL=http://consul:8500 \
    CONSUL_TOKEN=bOKGjerhQcCsjphbg3Wqg40r \
    VAULT_URL=http://vault:8200

RUN yum install -y docker;

RUN pip install docker docker-compose python-consul

COPY src /src
WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml -vv
