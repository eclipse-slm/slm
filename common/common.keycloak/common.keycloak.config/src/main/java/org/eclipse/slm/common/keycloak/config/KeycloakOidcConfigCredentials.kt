package org.eclipse.slm.common.keycloak.config

import com.fasterxml.jackson.annotation.JsonProperty

class KeycloakOidcConfigCredentials(

    @JsonProperty("secret")
    val secret: String

) {
}