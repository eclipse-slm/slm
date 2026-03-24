package org.eclipse.slm.resource_management.features.capabilities.jobs

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.Date
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CapabilityJobDTO(
    var id: UUID,

    var resourceId: UUID,

    var capabilityId: UUID,

    var skipInstall: Boolean,

    var configParameters: MutableMap<String, String> = mutableMapOf(),

    var state: CapabilityJobState? = CapabilityJobState.CREATED,

    var createdAt: Date = Date(),

    var stateTransitions: MutableList<CapabilityJobStateTransition> = mutableListOf(),

    var logMessages: MutableList<String> = mutableListOf()
){

}