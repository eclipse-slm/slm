package org.eclipse.slm.common.vault.client.exceptions

class VaultKvSecretsNotFoundException(kvPath: String) : RuntimeException("KV Secret not found at path: $kvPath") {

    constructor(kvPath: String, cause: Throwable) : this(kvPath) {
        initCause(cause)
    }

}
