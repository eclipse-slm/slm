package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class NodeServicesResponse (

    @field:JsonProperty("Node")
        var Node: Node,

    @field:JsonProperty("Services")
        var Services: List<NodeService>? = emptyList(),
)
