package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the Service Proxy configuration for a service in Consul.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#serviceproxy">Consul API docs</a>.
 */
data class ServiceProxy(

    @field:JsonProperty("DestinationServiceName")
    val destinationServiceName: String? = null,

    @field:JsonProperty("DestinationServiceID")
    val destinationServiceId: String? = null,

    @field:JsonProperty("LocalServiceAddress")
    val localServiceAddress: String? = null,

    @field:JsonProperty("LocalServicePort")
    val localServicePort: Int? = null,

    @field:JsonProperty("Config")
    val config: Any? = null,

    @field:JsonProperty("Upstreams")
    val upstreams: Any? = null

)