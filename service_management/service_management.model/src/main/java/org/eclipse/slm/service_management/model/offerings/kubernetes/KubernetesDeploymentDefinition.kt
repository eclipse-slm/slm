package org.eclipse.slm.service_management.model.offerings.kubernetes

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinition

@JsonTypeName("KUBERNETES")
class KubernetesDeploymentDefinition : DeploymentDefinition(DeploymentType.KUBERNETES) {

    @JsonProperty("manifestFile")
    var manifestFile = ""

}
