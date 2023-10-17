package org.eclipse.slm.service_management.service.rest.service_deployment

import org.keycloak.KeycloakPrincipal
import org.keycloak.KeycloakSecurityContext
import java.util.*

data class UndeploymentJobRun(

    val keycloakPrincipal: KeycloakPrincipal<KeycloakSecurityContext>,

    val serviceInstanceId: UUID,

    val resourceId: UUID

    )
