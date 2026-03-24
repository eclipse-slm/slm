package org.eclipse.slm.common.consul.client.auth

/**
 * Consul authentication using a native Consul token.
 */
data class ConsulTokenAuthentication(
    /** The Consul token to use for authentication. */
    val token: String
) : ConsulAuthentication(ConsulAuthenticationType.TOKEN) {

    /**
     * Get the Consul token.
     * @return The Consul token.
     */
    override fun getConsulToken(): String = token
}