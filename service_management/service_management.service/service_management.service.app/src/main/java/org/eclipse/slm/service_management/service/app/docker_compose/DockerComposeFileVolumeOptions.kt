package org.eclipse.slm.service_management.service.app.docker_compose

import com.fasterxml.jackson.annotation.JsonProperty

data class DockerComposeFileVolumeOptions(@JsonProperty val nocopy: Boolean) {
}
