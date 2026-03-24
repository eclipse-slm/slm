package org.eclipse.slm.common.vault.client.exceptions

class VaultGroupNotFoundException(groupName: String) : RuntimeException("Vault group with name '$groupName' not found") {

    constructor(groupName: String, cause: Throwable) : this(groupName) {
        initCause(cause)
    }
}
