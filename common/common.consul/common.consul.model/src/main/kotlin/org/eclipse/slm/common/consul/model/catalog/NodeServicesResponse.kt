package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty

data class NodeServicesResponse (

        @JsonProperty("Node")
        var Node: Node,

        @JsonProperty("Services")
        var Services: List<NodeService>?
)
