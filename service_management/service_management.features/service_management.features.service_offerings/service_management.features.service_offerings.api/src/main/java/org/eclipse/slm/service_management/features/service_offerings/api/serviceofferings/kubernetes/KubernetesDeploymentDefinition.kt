package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.kubernetes

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.DeploymentDefinition

@JsonTypeName("KUBERNETES")
class KubernetesDeploymentDefinition : DeploymentDefinition(DeploymentType.KUBERNETES) {

    @JsonProperty("manifestFile")
    var manifestFile = ""

}
