package org.eclipse.slm.common.consul.client.auth

import org.eclipse.slm.common.consul.client.apis.ConsulLoginApiClient
import org.eclipse.slm.common.restclient.feign.FeignClientFactory

/**
 * Consul authentication using JWT (JSON Web Token). A JWT is exchanged for a Consul token.
 */
data class ConsulJwtAuthentication(
    /** The URL of the Consul server */
    var consulUrl: String,
    /** The JWT token */
    val jwt: String
) : ConsulAuthentication(ConsulAuthenticationType.JWT) {

    /** Get the Consul token by exchanging the JWT
     * @return The Consul token
     */
    override fun getConsulToken(): String {
        if (!consulUrl.endsWith("/v1/")) {
            consulUrl += "/v1/"
        }

        val consulLoginApiClient = FeignClientFactory.createClient(ConsulLoginApiClient::class.java, consulUrl)
        val loginRequest = ConsulLoginRequest("keycloak", jwt, null, null)
        val authResponse = consulLoginApiClient.login(loginRequest)
        return "${authResponse.secretID}"
    }
}