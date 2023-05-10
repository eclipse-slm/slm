package org.eclipse.slm.service_management.service.rest.service_deployment

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver
import org.eclipse.slm.service_management.model.offerings.ServiceOrder
import org.eclipse.slm.service_management.model.services.ServiceInstance
import org.keycloak.KeycloakPrincipal
import org.keycloak.KeycloakSecurityContext

data class DeploymentJobRun(

    val awxJobObserver: AwxJobObserver,

    val keycloakPrincipal: KeycloakPrincipal<KeycloakSecurityContext>,

    val serviceInstance: ServiceInstance,

    val serviceOrder: ServiceOrder

    )
