package org.eclipse.slm.tests.utils

class KeycloakAuthException(message: String) : RuntimeException(message) {

    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }

}