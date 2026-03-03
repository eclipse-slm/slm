package org.eclipse.slm.common.vault.model.mounts

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a mounted secrets engine in Vault.
 *
 * https://developer.hashicorp.com/vault/api-docs/system/mounts#list-mounted-secrets-engines
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Mount(
    @field:JsonProperty("accessor")
    val accessor: String,

    @field:JsonProperty("config")
    val config: MountConfig,

    @field:JsonProperty("description")
    val description: String,

    @field:JsonProperty("external_entropy_access")
    val externalEntropyAccess: Boolean,

    @field:JsonProperty("local")
    val local: Boolean,

    @field:JsonProperty("options")
    val options: Any?,

    @field:JsonProperty("plugin_version")
    val pluginVersion: String,

    @field:JsonProperty("running_plugin_version")
    val runningPluginVersion: String,

    @field:JsonProperty("running_sha256")
    val runningSha256: String,

    @field:JsonProperty("seal_wrap")
    val sealWrap: Boolean,

    @field:JsonProperty("type")
    val type: String,

    @field:JsonProperty("uuid")
    val uuid: String
)