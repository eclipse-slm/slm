package org.eclipse.slm.common.keycloak.config

import com.fasterxml.jackson.annotation.JsonProperty

class KeycloakOidcConfig(

    @JsonProperty("realm")
    val realm: String,

    @JsonProperty("auth-server-url")
    val authServerUrl: String,

    @JsonProperty("ssl-required")
    val sslRequired: String,

    @JsonProperty("resource")
    val resource: String,

    @JsonProperty("credentials")
    val credentials: KeycloakOidcConfigCredentials,

    @JsonProperty("confidential-port")
    val confidentialPort: Int

) {

    val authServerUrlIncludingRealm: String = this.authServerUrl + "realms/" + this.realm;

    val tokenServerUrl: String = this.authServerUrlIncludingRealm + "/protocol/openid-connect/token"
}