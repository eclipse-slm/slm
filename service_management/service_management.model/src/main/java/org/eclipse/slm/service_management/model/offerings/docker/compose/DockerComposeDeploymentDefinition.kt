package org.eclipse.slm.service_management.model.offerings.docker.compose

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinition

@JsonTypeName("DOCKER_COMPOSE")
class DockerComposeDeploymentDefinition : DeploymentDefinition(DeploymentType.DOCKER_COMPOSE) {

    @JsonProperty("composeFile")
    var composeFile = ""

    @JsonProperty("dotEnvFile")
    var dotEnvFile = ""

    @JsonProperty("envFiles")
    var envFiles: Map<String, String> = HashMap()
}
