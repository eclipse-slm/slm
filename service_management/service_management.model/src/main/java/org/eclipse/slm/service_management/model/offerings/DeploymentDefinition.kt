package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.*
import org.eclipse.slm.common.model.DeploymentType
import org.eclipse.slm.service_management.model.offerings.codesys.CodesysDeploymentDefinition
import org.eclipse.slm.service_management.model.offerings.docker.compose.DockerComposeDeploymentDefinition
import org.eclipse.slm.service_management.model.offerings.docker.container.DockerContainerDeploymentDefinition
import org.eclipse.slm.service_management.model.offerings.kubernetes.KubernetesDeploymentDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.Column

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "deploymentType")
@JsonSubTypes(
    JsonSubTypes.Type(value = DockerContainerDeploymentDefinition::class, name = "DOCKER_CONTAINER"),
    JsonSubTypes.Type(value = DockerComposeDeploymentDefinition::class, name = "DOCKER_COMPOSE"),
    JsonSubTypes.Type(value = KubernetesDeploymentDefinition::class, name = "KUBERNETES"),
    JsonSubTypes.Type(value = CodesysDeploymentDefinition::class, name = "CODESYS"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class DeploymentDefinition(deploymentType: DeploymentType) {

    @Transient
    private val LOG: Logger = LoggerFactory.getLogger(DeploymentDefinition::class.java)

    @Column(name = "deployment_type")
    @JsonProperty("deploymentType")
    var deploymentType: DeploymentType = deploymentType
}
