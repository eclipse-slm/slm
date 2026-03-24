package org.eclipse.slm.common.vault.model.mounts

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SecretsEngine(
    @field:JsonProperty("type")
    val type: String? = null,

    @field:JsonProperty("description")
    val description: String? = null,

    @field:JsonProperty("config")
    val config: SecretsEngineConfig? = null,

    @field:JsonProperty("options")
    val options: KvSecretsEngineOptions? = null
)
