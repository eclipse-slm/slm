package org.eclipse.slm.service_management.service.rest.kubernetes

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonInclude(JsonInclude.Include.NON_EMPTY)

data class KubernetesManifestFile(

    @JsonProperty("raw")
    var raw: String,

    @JsonProperty("definitions")
    var definitions: List<KubernetesGenericObject>,

)
{
    fun toConcatenatedString(): String {
        return definitions.joinToString(separator = "---\n") { obj: KubernetesGenericObject -> obj.toString() }
    }
}
