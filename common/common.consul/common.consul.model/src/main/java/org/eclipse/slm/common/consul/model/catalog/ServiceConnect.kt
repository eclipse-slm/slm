package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the Service Connect configuration for a service in Consul.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#serviceconnect">Consul API docs</a>.
 */
data class ServiceConnect(

    /** Specifies whether Service Connect is enabled for the service. */
    @field:JsonProperty("Native")
    val nativeValue: Boolean? = null,

    /** Configuration for the Service Connect proxy. */
    @field:JsonProperty("Proxy")
    val proxy: Any? = null
)