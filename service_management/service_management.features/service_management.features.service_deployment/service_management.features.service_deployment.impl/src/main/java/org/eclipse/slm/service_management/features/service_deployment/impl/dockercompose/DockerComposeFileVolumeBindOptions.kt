package org.eclipse.slm.service_management.features.service_deployment.impl.dockercompose

import com.fasterxml.jackson.annotation.JsonProperty

data class DockerComposeFileVolumeBindOptions(@JsonProperty val propagation: String) {
}
