package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty

class TaggedAddresses {
    @JsonProperty("Lan")
    var lan: String? = null

    @JsonProperty("Wan")
    var wan: String? = null
}
