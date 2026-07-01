package org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose

enum class DockerComposeFileDependsCondition {
    service_started,
    service_healthy,
    service_completed_successfully
}
