package org.eclipse.slm.service_management.model.offerings.docker.compose

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.DeploymentDefinitionDTOFileImport
import java.util.*

@JsonTypeName("DOCKER_COMPOSE")
class DockerComposeDeploymentDefinitionDTOFileImport(id: UUID? = null) : DeploymentDefinitionDTOFileImport(DeploymentType.DOCKER_COMPOSE) {

    var composeFile = ""
    var composeFilename = ""

    var dotEnvFile = ""

    var envFiles: Map<String, String> = HashMap()
    var envFilenames: List<String> = listOf()

}
