package org.eclipse.slm.common.vault.model.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JwtGroupAliasCreateRequest(
    @field:JsonProperty("name")
    val name: String,

    @field:JsonProperty("mount_accessor")
    val mountAccessor: String,

    @field:JsonProperty("mount_path")
    val mountPath: String,

    @field:JsonProperty("mount_type")
    val mountType: String,

    @field:JsonProperty("canonical_id")
    val canonicalId: String,
)