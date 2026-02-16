package org.eclipse.slm.common.vault.client.exceptions

class VaultPermissionDeniedException(message: String) : RuntimeException() {

    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }

}
