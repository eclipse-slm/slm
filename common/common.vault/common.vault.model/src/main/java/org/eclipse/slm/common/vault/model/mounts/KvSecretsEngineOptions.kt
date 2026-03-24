package org.eclipse.slm.common.vault.model.mounts

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Represents the options for a KV (Key-Value) secrets engine in Vault.
 *
 * https://developer.hashicorp.com/vault/api-docs/system/mounts#version
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class KvSecretsEngineOptions(

    /** The version of the KV secrets engine (e.g., "1" or "2"). */
    var version: String
)
