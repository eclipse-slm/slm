package org.eclipse.slm.service_management.service.app.docker_compose

enum class DockerComposeFileDependsCondition {
    service_started,
    service_healthy,
    service_completed_successfully
}
