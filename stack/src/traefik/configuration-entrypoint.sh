#!/bin/sh

echo "[ Configuring Traefik... ]"

mkdir -p /certs

export CONSUL_HTTP_ADDR=$CONSUL_URL
export CONSUL_HTTP_TOKEN=$CONSUL_TOKEN

# Wait until Consul is running
until [ "$(curl -m 5 -s -k --location --request GET "$CONSUL_HTTP_ADDR/v1/status/leader")" = '"172.17.0.1:8300"' ]; do
  echo "Consul is unavailable -> sleeping"
  sleep 1
done

consul kv get certs/traefik/crt > /certs/certificate.crt
consul kv get certs/traefik/key > /certs/certificate.key

echo "[ Starting Traefik... ]"

# Forwarding to the base entrypoint
exec traefik "$@"