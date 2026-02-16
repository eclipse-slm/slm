package org.eclipse.slm.common.vault.client.exceptions

class VaultPolicyNotFoundException(policyName: String) : RuntimeException("Vault policy with name '$policyName' not found") {

    constructor(groupName: String, cause: Throwable) : this(groupName) {
        initCause(cause)
    }

}
