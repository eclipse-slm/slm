package org.eclipse.slm.service_management.service.rest.docker_compose

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DockerComposeFileService(
    @JsonProperty("image")
    var image: String,

    @JsonProperty("restart")
    var restart: String?,

    @JsonProperty("privileged")
    var privileged: Boolean?,

    @JsonProperty("ports")
    var ports: List<String>? = ArrayList(),

    @JsonProperty("env_file")
    var envFiles: List<String>? = ArrayList(),

    @JsonProperty("environment")
    @JsonDeserialize(contentUsing = ArrayMapNodeDeserializer::class)
    var environment: DockerComposeFileEnvironment = DockerComposeFileEnvironment(),

    @JsonProperty("volumes")
    @JsonSerialize(using = DockerComposeFileVolumeListSerializer::class)
    var volumes: List<DockerComposeFileVolume>? = ArrayList(),

    @JsonProperty("networks")
    var networks: Map<String, Object>? = HashMap(),

    @JsonProperty("labels")
    @JsonDeserialize(contentUsing = ArrayMapNodeDeserializer::class)
    var labels: DockerComposeFileLabels = DockerComposeFileLabels(),

    @JsonProperty("build")
    var build: String?,

    @JsonProperty("logging")
    var logging: Map<String, Object>?,

    @JsonProperty("healthcheck")
    var healthCheck: Map<String, Object>?,

    @JsonProperty("depends_on")
    var dependsOn: List<String>?,

    @JsonProperty("devices")
    var devices: List<String>?,

    @JsonProperty("network_mode")
    var networkMode: String?,

    @JsonProperty("extra_hosts")
    var extraHosts: List<String>?,
)
