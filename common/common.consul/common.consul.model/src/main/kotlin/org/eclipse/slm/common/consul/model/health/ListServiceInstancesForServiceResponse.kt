package org.eclipse.slm.common.consul.model.health

import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.slm.common.consul.model.catalog.CatalogNode

class ListServiceInstancesForServiceResponse {

    @JsonProperty("Node")
    var node: CatalogNode? = null

    @JsonProperty("Service")
    var service: CatalogNode.Service? = null

    @JsonProperty("Checks")
    var checks: List<CatalogNode.Check>? = null

}
