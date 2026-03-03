package org.eclipse.slm.common.vault.model.kv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class KvMetadataCreateRequest(

    @field:JsonProperty("custom_metadata") val metadata: Map<String, String>

)