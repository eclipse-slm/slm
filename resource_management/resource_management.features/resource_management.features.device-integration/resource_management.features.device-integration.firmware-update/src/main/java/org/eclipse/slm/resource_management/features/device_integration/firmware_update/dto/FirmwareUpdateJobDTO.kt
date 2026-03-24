package org.eclipse.slm.resource_management.features.device_integration.firmware_update.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobState
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobStateTransition
import java.util.Date
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FirmwareUpdateJobDTO(
    var id: UUID,

    var resourceId: UUID,

    var driverId: String,

    var softwareNameplateId: String,

    var state: FirmwareUpdateJobState,

    var createdAt: Date,

    var stateTransitions: MutableList<FirmwareUpdateJobStateTransition> = mutableListOf(),

    var logMessages: MutableList<String> = mutableListOf()
){

}