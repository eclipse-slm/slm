package org.eclipse.slm.service_management.service.rest.docker_compose

import com.fasterxml.jackson.annotation.JsonValue

enum class DockerComposeFileVolumeType(@JsonValue val volumeType: String) {
    VOLUME("volume"),
    BIND("bind"),
    TMPFS("tmpfs"),
    NPIPE("npipe")
}
