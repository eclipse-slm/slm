package org.eclipse.slm.service_management.features.service_deployment.impl.dockercompose

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DockerComposeFile(

    @param:JsonProperty("services")
    var services: Map<String, DockerComposeFileService>,

    @param:JsonProperty("networks")
    var networks: Map<String, Object>?,

    @JsonSerialize(using = DockerComposeFileVolumeMapSerializer::class)
    @param:JsonProperty("volumes")
    var volumes: Map<String, Object>?
)
{
    fun toJsonString(): String {
        val objectMapper = ObjectMapper()
        objectMapper.registerKotlinModule()
        return objectMapper.writeValueAsString(this)
    }
}
