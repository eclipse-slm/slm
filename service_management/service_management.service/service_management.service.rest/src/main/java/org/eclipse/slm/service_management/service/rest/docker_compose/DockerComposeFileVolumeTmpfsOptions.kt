package org.eclipse.slm.service_management.service.rest.docker_compose

import com.fasterxml.jackson.annotation.JsonProperty

data class DockerComposeFileVolumeTmpfsOptions(@JsonProperty val size: Long) {
}
