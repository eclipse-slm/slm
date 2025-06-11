FROM traefik:v3.3.4

ENV CONSUL_URL="http://consul:8500" \
    CONSUL_TOKEN="your-consul-token"

ARG CONSUL_VERSION=1.20.5

RUN apk --no-cache add curl

RUN curl -sSLo /tmp/consul.zip https://releases.hashicorp.com/consul/${CONSUL_VERSION}/consul_${CONSUL_VERSION}_linux_amd64.zip && \
    unzip /tmp/consul.zip -d /bin && \
    rm /tmp/consul.zip

ADD configuration-entrypoint.sh /configuration-entrypoint.sh
RUN chmod +x /configuration-entrypoint.sh

RUN mkdir -p /certs

ENTRYPOINT ["/configuration-entrypoint.sh"]
CMD ["traefik"]