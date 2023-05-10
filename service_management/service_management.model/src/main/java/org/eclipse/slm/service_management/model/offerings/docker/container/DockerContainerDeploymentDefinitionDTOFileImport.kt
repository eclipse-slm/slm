package org.eclipse.slm.service_management.model.offerings.docker.container

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinitionDTOFileImport
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingEnvironmentVariable
import java.util.*

@JsonTypeName("DOCKER_CONTAINER")
open class DockerContainerDeploymentDefinitionDTOFileImport(id: UUID? = null) : DeploymentDefinitionDTOFileImport(DeploymentType.DOCKER_CONTAINER) {

    var imageRepository = ""

    var imageTag = ""

    var restartPolicy = "Always"

    var environmentVariables: List<ServiceOfferingEnvironmentVariable> = ArrayList()

    var labels: List<DockerContainerLabel> = ArrayList()

    var portMappings: List<DockerContainerPortMapping> = ArrayList()

    var volumes: List<DockerContainerVolume> = ArrayList()

}
