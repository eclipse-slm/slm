package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.codesys.CodesysDeploymentDefinitionDTOFileImport
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.compose.DockerComposeDeploymentDefinitionDTOFileImport
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.container.DockerContainerDeploymentDefinitionDTOFileImport
import org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.kubernetes.KubernetesDeploymentDefinitionDTOFileImport

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
