package org.eclipse.slm.common.vault.model.kv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class KvSecretsMetadata (
    @param:JsonProperty("created_time")
    val createdTime: Date,

    @param:JsonProperty("deletion_time")
    val deletionTime: Date?,

    @param:JsonProperty("custom_metadata")
    val customMetadata: MutableMap<String, String>?,

    @param:JsonProperty("destroyed")
    val destroyed: Boolean,

    @param:JsonProperty("version")
    val version: Integer,
)