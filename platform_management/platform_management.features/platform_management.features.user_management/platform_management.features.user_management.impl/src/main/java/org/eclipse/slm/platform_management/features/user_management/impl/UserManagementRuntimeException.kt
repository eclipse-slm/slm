package org.eclipse.slm.platform_management.features.user_management.impl

class UserManagementRuntimeException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

