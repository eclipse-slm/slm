package org.eclipse.slm.common.vault.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A generic class to represent a response received from Vault HTTP API where the `data` field can be of any type.
 */
data class VaultApiResponse<T>(
    @field:JsonProperty("request_id") val requestId: String?,

    @field:JsonProperty("lease_id") val leaseId: String?,

    @field:JsonProperty("renewable") val renewable: Boolean?,

    @field:JsonProperty("lease_duration") val leaseDuration: Int?,

    @field:JsonProperty("data") val data: T?,

    @field:JsonProperty("wrap_info") val wrapInfo: Any?,

    @field:JsonProperty("warnings") val warnings: List<String>?,

    @field:JsonProperty("auth") val auth: T?,

    @field:JsonProperty("mount_type") val mountType: String?
)