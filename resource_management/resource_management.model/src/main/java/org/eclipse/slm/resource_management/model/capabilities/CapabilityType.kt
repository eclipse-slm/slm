package org.eclipse.slm.resource_management.model.capabilities

import org.eclipse.slm.resource_management.model.capabilities.actions.CapabilityActionType


enum class CapabilityType(val actions: MutableList<CapabilityActionType>) {

    SETUP(
        mutableListOf(
            CapabilityActionType.INSTALL,
            CapabilityActionType.UNINSTALL
        )
    ),

    DEPLOY(
        mutableListOf(
            CapabilityActionType.DEPLOY,
            CapabilityActionType.UNDEPLOY
        )
    ),

    SCALE(
        mutableListOf(
            CapabilityActionType.SCALE_UP,
            CapabilityActionType.SCALE_DOWN
        )
    ),

    VM(
        mutableListOf(
            CapabilityActionType.CREATE_VM,
            CapabilityActionType.DELETE_VM
        )
    )
}
