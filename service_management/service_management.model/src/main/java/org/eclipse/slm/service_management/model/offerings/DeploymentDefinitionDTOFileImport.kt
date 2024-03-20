package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.codesys.CodesysDeploymentDefinitionDTOFileImport
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinitionDTOFileImport
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinitionDTOFileImport
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinitionDTOFileImport

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "deploymentType")
@JsonSubTypes(
    JsonSubTypes.Type(value = DockerContainerDeploymentDefinitionDTOFileImport::class, name = "DOCKER_CONTAINER"),
    JsonSubTypes.Type(value = DockerComposeDeploymentDefinitionDTOFileImport::class, name = "DOCKER_COMPOSE"),
    JsonSubTypes.Type(value = KubernetesDeploymentDefinitionDTOFileImport::class, name = "KUBERNETES"),
    JsonSubTypes.Type(value = CodesysDeploymentDefinitionDTOFileImport::class, name = "CODESYS"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class DeploymentDefinitionDTOFileImport(deploymentType: DeploymentType) {

    var deploymentType: DeploymentType = deploymentType

}
