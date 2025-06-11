FROM ghcr.io/eclipse-slm/ansible:2.17.2

ENV CONFIGURE_CORE_PROJECT=false \
    LOCAL_HOST=172.17.0.1

COPY src /src
COPY templates /templates

WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml -v
