package org.eclipse.slm.resource_management.common.remote_access

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class RemoteAccessCreateDTO (

    /** The full path owner group id which will have access to the created remote access. */
    @param:JsonProperty("fullPathOwnerGroupId")
    val fullPathOwnerGroupId: String,

    /** The id of an existing credential to use for remote access. */
    @param:JsonProperty("credentialId")
    val credentialId: UUID,

    /**
     * The usernamed used to for remote access. It is only required for credentials of type KEY_PAIR and will be ignored for credentials of
     * type USERNAME_PASSWORD.
     */
    @param:JsonProperty("username")
    val username: String?,

    /** The port to connect for remote access. */
    @param:JsonProperty("connectionPort")
    val connectionPort: Int,

    /** The type of connection for remote access. */
    @param:JsonProperty("connectionType")
    val connectionType: ConnectionType

)
