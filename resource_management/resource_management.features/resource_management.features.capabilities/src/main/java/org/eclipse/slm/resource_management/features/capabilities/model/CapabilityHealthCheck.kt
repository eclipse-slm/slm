package org.eclipse.slm.resource_management.features.capabilities.model

import com.fasterxml.jackson.annotation.JsonProperty

class CapabilityHealthCheck(

    @JsonProperty("type")
    val type: CapabilityHealthCheckType,

    @JsonProperty("port")
    val port: Int,

    @JsonProperty("path")
    val path: String,

    // Interval in seconds
    @JsonProperty("interval")
    val interval: Int
)
{
    fun getUrl(host : String): String {
        return if(path[0]!= '/')
            "http://$host:$port/$path";
        else
            "http://$host:$port$path";
    }
}
