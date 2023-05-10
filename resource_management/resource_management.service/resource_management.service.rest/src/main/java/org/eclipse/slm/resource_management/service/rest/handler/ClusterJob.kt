package org.eclipse.slm.resource_management.service.rest.handler

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver
import org.eclipse.slm.resource_management.model.cluster.ClusterCreateRequest
import org.eclipse.slm.resource_management.model.consul.capability.MultiHostCapabilityService
import org.eclipse.slm.resource_management.model.consul.capability.ScaleOperation
import org.keycloak.KeycloakPrincipal

class ClusterJob(
    var multiHostCapabilityService: MultiHostCapabilityService,
) {
    var awxJobObserver: AwxJobObserver? = null

    var scaleOperation: ScaleOperation? = null

    var keycloakPrincipal: KeycloakPrincipal<*>? = null

    var clusterCreateRequest: ClusterCreateRequest? = null

    constructor(
        multiHostCapabilityService: MultiHostCapabilityService,
        awxJobObserver: AwxJobObserver,
        scaleOperation: ScaleOperation?,
        keycloakPrincipal: KeycloakPrincipal<*>,
        clusterCreateRequest: ClusterCreateRequest
    ) : this(multiHostCapabilityService) {
        this.awxJobObserver = awxJobObserver
        this.scaleOperation = scaleOperation
        this.keycloakPrincipal = keycloakPrincipal
        this.clusterCreateRequest = clusterCreateRequest
    }
}
