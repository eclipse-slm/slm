FROM fabos4ai/ansible-core:2.11.9

ENV CONFIGURE_CORE_PROJECT=false

COPY src /src
COPY templates /templates

WORKDIR src

CMD ansible-playbook main.yml
