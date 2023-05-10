package org.eclipse.slm.service_management.service.rest.docker_compose

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_EMPTY)

data class DockerComposeFile(
    @JsonProperty("version")
    var version: String,

    @JsonProperty("services")
    var services: Map<String, DockerComposeFileService>,

    @JsonProperty("networks")
    var networks: Map<String, Object>?,

    @JsonSerialize(using = DockerComposeFileVolumeMapSerializer::class)
    @JsonProperty("volumes")
    var volumes: Map<String, Object>?
)
{
    fun toJsonString(): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(this)
    }
}
