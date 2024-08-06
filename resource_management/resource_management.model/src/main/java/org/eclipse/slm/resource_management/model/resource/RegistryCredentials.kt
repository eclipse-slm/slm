package org.eclipse.slm.resource_management.model.resource

class RegistryCredentials {
    var id: Int? = null
    var description: String? = null
    var authenticationURL: String = "quary.io"
    var username: String = ""
    var password: String = ""
    var verifySSL: Boolean = true
}