#!/bin/bash
echo "Entrypoint läuft"
set -e

echo "Get SLM Root CA cert from Vault and add to system trust store..."
curl -s http://vault:8200/v1/pki_root_slm/cert/ca | jq -r .data.certificate > /usr/local/share/ca-certificates/slm_root_ca.crt
update-ca-certificates

exec "$@"