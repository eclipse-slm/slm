package org.eclipse.slm.resource_management.model.resource

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("CredentialUsernamePassword")
class CredentialUsernamePassword() : Credential() {
    var password: String = ""
    var username: String = ""
    constructor(username: String, password: String) : this() {
        this.username = username
        this.password = password
    }
}
