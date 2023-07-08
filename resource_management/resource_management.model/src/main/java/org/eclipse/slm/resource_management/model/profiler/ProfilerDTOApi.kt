package org.eclipse.slm.resource_management.model.profiler

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.eclipse.slm.resource_management.model.actions.Action
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ProfilerDTOApi(
    val name: String,
    val action: Action,
) {
    val id: UUID? = null
}
