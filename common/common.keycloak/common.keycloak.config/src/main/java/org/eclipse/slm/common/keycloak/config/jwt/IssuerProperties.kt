package org.eclipse.slm.common.keycloak.config.jwt

import java.net.URL

class IssuerProperties {
    var uri: URL? = null

    var claims: Array<ClaimMappingProperties> = arrayOf(
        ClaimMappingProperties().apply {
            jsonPath = "$.realm_access.roles"
        },
        ClaimMappingProperties().apply {
            jsonPath = "$.resource_access.*.roles"
        })

    var usernameJsonPath: String = "$.preferred_username"
}