package org.eclipse.slm.common.consul.client

import org.keycloak.KeycloakPrincipal

class ConsulCredential(
    val consulCredentialType: ConsulCredentialType)
{
    var consulToken: String? = null

    var keycloakPrincipal: KeycloakPrincipal<*>? = null

    constructor() : this(ConsulCredentialType.APPLICATION_PROPERTIES) {
    }

    constructor(consulToken: String) : this(ConsulCredentialType.CONSUL_TOKEN) {
        this.consulToken = consulToken
    }

    constructor(keycloakPrincipal: KeycloakPrincipal<*>) : this(ConsulCredentialType.KEYCLOAK_TOKEN) {
        this.keycloakPrincipal = keycloakPrincipal
    }
}
