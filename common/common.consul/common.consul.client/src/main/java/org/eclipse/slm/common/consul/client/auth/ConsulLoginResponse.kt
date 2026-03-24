package org.eclipse.slm.common.consul.client.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the response received after a successful login to Consul's ACL system.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl#sample-response-2">Consul API docs</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConsulLoginResponse(

    /** The accessor ID of the token. */
    @field:JsonProperty("AccessorID")
    val accessorID: String?,

    /** The secret ID of the token. */
    @field:JsonProperty("SecretID")
    val secretID: String?,

    /** The description of the token. */
    @field:JsonProperty("Description")
    val description: String?,

    /** The roles associated with the token. */
    @field:JsonProperty("Roles")
    val roles: List<Role>?,

    /** The service identities associated with the token. */
    @field:JsonProperty("ServiceIdentities")
    val serviceIdentities: List<ServiceIdentity>?,

    /** Indicates if the token is local to the agent. */
    @field:JsonProperty("Local")
    val local: Boolean,

    /** The authentication method used for login. */
    @field:JsonProperty("AuthMethod")
    val authMethod: String?,

    /** The time the token was created. */
    @field:JsonProperty("CreateTime")
    val createTime: String?,

    /** The hash of the token. */
    @field:JsonProperty("Hash")
    val hash: String?,
) {

    /** Represents a role associated with the token. */
    data class Role(

        /** The ID of the role. */
        @field:JsonProperty("ID")
        val id: String?,

        /** The name of the role. */
        @field:JsonProperty("Name")
        val name: String?
    )

    /** Represents a service identity associated with the token. */
    data class ServiceIdentity(
        /** The name of the service. */
        @field:JsonProperty("ServiceName")
        val serviceName: String?
    )
}