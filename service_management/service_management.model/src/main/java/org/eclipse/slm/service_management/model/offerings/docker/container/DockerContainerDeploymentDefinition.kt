package org.eclipse.slm.service_management.model.offerings.docker.container

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinition
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingEnvironmentVariable

@JsonTypeName("DOCKER_CONTAINER")
open class DockerContainerDeploymentDefinition : DeploymentDefinition(DeploymentType.DOCKER_CONTAINER) {

    @JsonProperty("imageRepository")
    var imageRepository = ""

    @JsonProperty("imageTag")
    var imageTag = ""

    @JsonProperty("restartPolicy")
    open var restartPolicy: DockerRestartPolicy = DockerRestartPolicy.ALWAYS

    @JsonProperty("environmentVariables")
    var environmentVariables: List<ServiceOfferingEnvironmentVariable> = mutableListOf()

    @JsonProperty("labels")
    var labels: List<DockerContainerLabel> = mutableListOf()

    @JsonProperty("portMappings")
    var portMappings: List<DockerContainerPortMapping> = mutableListOf()

    @JsonProperty("volumes")
    var volumes: List<DockerContainerVolume> = mutableListOf()
}
