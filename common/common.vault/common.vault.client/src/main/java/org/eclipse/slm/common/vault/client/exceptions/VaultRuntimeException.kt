package org.eclipse.slm.common.vault.client.exceptions

class VaultRuntimeException(message: String) : RuntimeException(message) {

    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }

}
