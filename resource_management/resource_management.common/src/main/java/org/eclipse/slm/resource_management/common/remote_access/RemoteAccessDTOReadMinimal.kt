package org.eclipse.slm.resource_management.common.remote_access

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * Remote access data transfer object for read operations with minimal information. This DTO is used for listing remote accesses without detailed
 * credential information.
 */
class RemoteAccessDTOReadMinimal (

    /** Unique remote access identifier. */
    @field:JsonProperty("id")
    var id: UUID,

    /** Linked credential identifier. */
    @field:JsonProperty("credentialId")
    val credentialId: UUID,

    /** Username for remote access. Only available if {@link ConnectionType} is of type KEY_PAIR. */
    @field:JsonProperty("username")
    val username: String? = null,

    /** Remote access connection port. */
    @field:JsonProperty("connectionPort")
    val connectionPort: Int,

    /** Remote access connection type. */
    @field:JsonProperty("connectionType")
    val connectionType: ConnectionType

) {
}
