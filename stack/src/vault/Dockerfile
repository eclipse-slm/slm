FROM hashicorp/vault:1.21.2

RUN apk update && \
    apk upgrade && \
    apk add bash curl jq ca-certificates

COPY config.json /etc/vault.d/
COPY startup.sh /opt/vault/startup.sh

RUN chmod +x /opt/vault/startup.sh

CMD ["sh", "/opt/vault/startup.sh"]
