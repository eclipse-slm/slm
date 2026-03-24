package org.eclipse.slm.common.consul.client.auth

/**
 * Base class for Consul authentication methods.
 */
abstract class ConsulAuthentication(
    /** The type of authentication */
    val authenticationType: ConsulAuthenticationType
) {
    /** Get the Consul token for authentication
     * @return The Consul token
     */
    abstract fun getConsulToken(): String
}