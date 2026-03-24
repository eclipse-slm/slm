package org.eclipse.slm.resource_management.features.capabilities.clusters.handler

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver
import org.eclipse.slm.resource_management.features.capabilities.clusters.MultiHostCapabilityService
import org.eclipse.slm.resource_management.features.capabilities.clusters.ScaleOperation
import org.eclipse.slm.resource_management.features.capabilities.clusters.model.ClusterCreateRequest
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class ClusterJob(
    var multiHostCapabilityService: MultiHostCapabilityService,
) {
    var awxJobObserver: AwxJobObserver? = null

    var scaleOperation: ScaleOperation? = null

    var jwtAuthenticationToken: JwtAuthenticationToken? = null

    var clusterCreateRequest: ClusterCreateRequest? = null

    constructor(
        multiHostCapabilityService: MultiHostCapabilityService,
        awxJobObserver: AwxJobObserver,
        scaleOperation: ScaleOperation?,
        jwtAuthenticationToken: JwtAuthenticationToken,
        clusterCreateRequest: ClusterCreateRequest
    ) : this(multiHostCapabilityService) {
        this.awxJobObserver = awxJobObserver
        this.scaleOperation = scaleOperation
        this.jwtAuthenticationToken = jwtAuthenticationToken
        this.clusterCreateRequest = clusterCreateRequest
    }
}
