package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ConsulService (

    @JsonProperty("Node")
    var nodeId: UUID,

    @JsonProperty("ServiceName")
    var serviceName: String,

    @JsonProperty("ServiceID")
    var serviceId: UUID,

    @JsonProperty("ServiceTags")
    var serviceTags: List<String>,

    @JsonProperty("ServiceMeta")
    var serviceMetaData: Map<String, String>
)
{
}
