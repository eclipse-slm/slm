package org.eclipse.slm.common.consul.client.auth

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a request to log in to Consul using a specific authentication method.
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/acl#json-request-body-schema">Consul API docs</a>.
 */
data class ConsulLoginRequest(

    /** The name of the auth method to use for login. */
    @field:JsonProperty("AuthMethod")
    val authMethod: String?,

    /** The bearer token to present to the auth method during login for authentication purposes. */
    @field:JsonProperty("BearerToken")
    val bearerToken: String?,

    /** Specifies arbitrary KV metadata linked to the token. Can be useful to track origins. */
    @field:JsonProperty("Meta")
    val meta: Map<String, String>?,

    /**
     * <ENTERPRISE FEATURE> Specifies the namespace of the auth method you use to login. This field takes precedence over the ns query parameter, one of
     * several other methods to specify the namespace.
     */
    @field:JsonProperty("Namespace")
    val namespace: String?
)