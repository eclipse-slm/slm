package org.eclipse.slm.resource_management.features.profiler

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ProfilerDTOApi(
    id: UUID? = null,
) {
    var id: UUID = id ?: UUID.randomUUID()
    var name: String = ""
    var action: Action? = null
}
