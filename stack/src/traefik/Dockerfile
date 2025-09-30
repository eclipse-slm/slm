FROM traefik:v3.3.4

ENV SLM_HOSTNAME="your-slm-hostname" \
    VAULT_APP_ROLE_ROLE_ID="your-vault-app-role-role-id" \
    VAULT_APP_ROLE_SECRET_ID="your-vault-app-role-secret-id"

ARG VAULT_VERSION=1.20.3

# Install APK packages
RUN apk --no-cache add curl nano
# Install Vault
RUN curl -sSLo /tmp/vault.zip https://releases.hashicorp.com/vault/${VAULT_VERSION}/vault_${VAULT_VERSION}_linux_amd64.zip && \
    unzip -o /tmp/vault.zip -d /bin && \
    rm /tmp/vault.zip
# Copy config for Vault Agent
ADD vault vault
# Configure Entrypoint
ADD configuration-entrypoint.sh /configuration-entrypoint.sh
RUN chmod +x /configuration-entrypoint.sh
# Copy config for certs
ADD certs certs

ENTRYPOINT ["/configuration-entrypoint.sh"]
CMD ["traefik"]