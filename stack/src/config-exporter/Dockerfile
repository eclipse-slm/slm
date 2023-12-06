FROM fabos4ai/ansible-core:2.11.9

ENV CONFIGURE_CORE_PROJECT=false \
    LOCAL_HOST=172.17.0.1

RUN pip install docker docker-compose python-consul

COPY src /src
COPY templates /templates

WORKDIR src

RUN ansible-galaxy install -r requirements.yml

CMD ansible-playbook main.yml -v
