package org.eclipse.slm.common.vault.model.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JwtGroupAlias(
    @field:JsonProperty("canonical_id")
    val canonicalId: String?,

    @field:JsonProperty("creation_time")
    val creationTime: Date?,

    @field:JsonProperty("id")
    val id: String?,

    @field:JsonProperty("last_update_time")
    val last_update_time: Date?,

    @field:JsonProperty("merged_from_canonical_ids")
    val mergedFromCanonicalIds: String?,

    @field:JsonProperty("metadata")
    val metadata: Map<String,String>?,

    @field:JsonProperty("mount_accessor")
    val mountAccessor: String?,

    @field:JsonProperty("mount_path")
    val mountPath: String?,

    @field:JsonProperty("mount_type")
    val mountType: String?,

    @field:JsonProperty("name")
    val name: String?,
)