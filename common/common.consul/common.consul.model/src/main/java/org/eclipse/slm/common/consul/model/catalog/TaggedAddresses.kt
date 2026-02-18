package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty

/** Represents the tagged addresses of a node or service in Consul.
 *
 * For more information see <a href="https://developer.hashicorp.com/consul/api-docs/catalog#taggedaddresses">Consul API docs</a>.
 */
data class TaggedAddresses (

    /** The LAN address */
    @field:JsonProperty("Lan")
    var lan: String? = null,

    /** The WAN address */
    @field:JsonProperty("Wan")
    var wan: String? = null
)
