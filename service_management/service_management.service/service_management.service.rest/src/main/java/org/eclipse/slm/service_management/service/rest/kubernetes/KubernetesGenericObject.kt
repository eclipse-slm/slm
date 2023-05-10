package org.eclipse.slm.service_management.service.rest.kubernetes

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.kubernetes.client.util.Yaml

@JsonInclude(JsonInclude.Include.NON_EMPTY)

data class KubernetesGenericObject(

        @JsonProperty("raw")
        var raw: String,

        @JsonProperty("apiVersion")
        var apiVersion: String,

        @JsonProperty("kind")
        var kind: String,

        @JsonProperty("parsed")
        var parsed: Any?,

        )
{
    constructor(raw: String, apiVersion: String, kind: String): this(raw, apiVersion, kind, null) {  }

    override fun toString(): String {
        return if (parsed == null){
            raw
        } else {
            Yaml.dump(parsed)
        }
    }
}
