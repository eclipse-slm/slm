package org.eclipse.slm.service_management.service.rest.docker_compose

import com.fasterxml.jackson.annotation.JsonValue

enum class DockerComposeFileDependsOnConditionType(@JsonValue val value: String) {
    SERVICE_STARTED("service_started"),
    SERVICE_HEALTHY("service_healthy"),
    SERVICE_COMPLETED_SUCCESSFULLY("service_completed_successfully"),
}
