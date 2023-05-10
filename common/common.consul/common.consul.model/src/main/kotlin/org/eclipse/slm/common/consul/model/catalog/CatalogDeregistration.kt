package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class CatalogDeregistration {

    @JsonProperty("Datacenter")
    var datacenter: String? = null

    @JsonProperty("Node")
    var node: String? = null

    @JsonProperty("CheckID")
    var checkId: String? = null

    @JsonProperty("ServiceID")
    var serviceId: String? = null
    override fun toString(): String {
        return "CatalogDeregistration { datacenter='${datacenter}', node='${node}, checkId='${checkId}, serviceId='${serviceId}' }";
    }

}
