package org.eclipse.slm.common.keycloak.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class KeycloakOidcConfig @JsonCreator constructor(

    @field:JsonProperty("realm")
    val realm: String,

    @field:JsonProperty("auth-server-url")
    val authServerUrl: String,

    @field:JsonProperty("ssl-required")
    val sslRequired: String,

    @field:JsonProperty("resource")
    val resource: String,

    @field:JsonProperty("credentials")
    val credentials: KeycloakOidcConfigCredentials,

    @field:JsonProperty("confidential-port")
    val confidentialPort: Int

) {
    val authServerUrlIncludingRealm: String = this.authServerUrl + "realms/" + this.realm;

    val tokenServerUrl: String = this.authServerUrlIncludingRealm + "/protocol/openid-connect/token"
}