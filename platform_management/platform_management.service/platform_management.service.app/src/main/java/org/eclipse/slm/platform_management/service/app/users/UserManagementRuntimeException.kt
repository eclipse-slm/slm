package org.eclipse.slm.platform_management.service.app.users

class UserManagementRuntimeException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}