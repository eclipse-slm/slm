package org.eclipse.slm.common.consul.client.auth

/**
 * Enumeration of Consul authentication types.
 */
enum class ConsulAuthenticationType {

    /** Authentication using nativ Consul token.  */
    TOKEN,
    /** Authentication using JWT.  */
    JWT,

}
