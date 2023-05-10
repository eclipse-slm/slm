package org.eclipse.slm.common.vault.client

import org.eclipse.slm.common.vault.client.exceptions.InvalidVaultCredentialConfig

class VaultCredential (
    val vaultCredentialType: VaultCredentialType) {

    var token: String? = null

    constructor() : this(VaultCredentialType.APPLICATION_PROPERTIES) {
    }

    constructor(vaultCredentialType: VaultCredentialType, token: String) : this(vaultCredentialType) {
        if (vaultCredentialType == VaultCredentialType.CONSUL_TOKEN
            || vaultCredentialType == VaultCredentialType.KEYCLOAK_TOKEN) {
            this.token = token;
        }
        else {
            throw InvalidVaultCredentialConfig("Token string provided, but VaultCredentialType is not " +
                    "'VaultCredentialType.CONSUL_TOKEN' or 'VaultCredentialType.KEYCLOAK_TOKEN' (was '${vaultCredentialType}'");
        }
    }
}

