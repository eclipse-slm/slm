package org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose

import com.fasterxml.jackson.annotation.JsonProperty

data class DockerComposeFileVolumeBindOptions(@JsonProperty val propagation: String) {
}
