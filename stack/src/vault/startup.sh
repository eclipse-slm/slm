#! /bin/bash

VAULT_ADDR=http://127.0.0.1:8200
export VAULT_ADDR="$VAULT_ADDR"
export VAULT_API_ADDR="$VAULT_ADDR"

vault server -config /etc/vault.d &
export VAULT_PID=$!
echo "Vault PID: $VAULT_PID"

sleep 5s

CONFIG_ROOT_DIRECTORY=/vault/config/root
INIT_MSG_FILE=$CONFIG_ROOT_DIRECTORY/vault_init_msg
if [ -f "$INIT_MSG_FILE" ]; then
    echo "Vault already initialized"
else
    mkdir -p $CONFIG_ROOT_DIRECTORY
    vault operator init -address=$VAULT_ADDR -key-shares=3 -key-threshold=2 > $INIT_MSG_FILE
fi

VAULT_TOKEN=$(grep "Token" $INIT_MSG_FILE | cut -d: -f2 | xargs)
echo "$VAULT_TOKEN" > $CONFIG_ROOT_DIRECTORY/root_token

vault status -address=$VAULT_ADDR
SEAL_STATUS=$?

echo $SEAL_STATUS

if [ $SEAL_STATUS -eq 2 ]; then
  echo "Unsealing Vault..."
  vault operator unseal $(grep "Key 1" $INIT_MSG_FILE | cut -d: -f2 | xargs)
  vault operator unseal $(grep "Key 2" $INIT_MSG_FILE | cut -d: -f2 | xargs)
fi

echo "Get SLM Root CA cert from Vault and add to system trust store..."
mkdir -p /usr/local/share/ca-certificates /usr/share/ca-certificates/
curl http://localhost:8200/v1/pki_root_slm/cert/ca | jq -r .data.certificate > /usr/local/share/ca-certificates/slm_root_ca.crt
update-ca-certificates

wait -n

exit $?
