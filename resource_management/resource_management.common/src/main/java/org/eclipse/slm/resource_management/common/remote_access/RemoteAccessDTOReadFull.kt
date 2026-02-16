package org.eclipse.slm.resource_management.common.remote_access

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.credentials.model.CredentialReadDTO
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
/**
 * Remote access data transfer object for read operations with full information. This DTO is used for listing remote accesses with detailed
 * credential information.
 */
class RemoteAccessDTOReadFull (

    /** Unique remote access identifier. */
    @field:JsonProperty("id")
    var id: UUID,

    /** Linked credential. */
    @field:JsonProperty("credential")
    val credential: CredentialReadDTO,

    /** Username for remote access. */
    @field:JsonProperty("username")
    val username: String,

    /** Remote access connection port. */
    @field:JsonProperty("connectionPort")
    val connectionPort: Int,

    /** Remote access connection type. */
    @field:JsonProperty("connectionType")
    val connectionType: ConnectionType

) {
}
