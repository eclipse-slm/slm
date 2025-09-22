package org.eclipse.slm.resource_management.features.capabilities.model

import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType


enum class CapabilityType(val actions: MutableList<ActionType>) {

    SETUP(
        mutableListOf(
            ActionType.INSTALL,
            ActionType.UNINSTALL
        )
    ),

    DEPLOY(
        mutableListOf(
            ActionType.DEPLOY,
            ActionType.UNDEPLOY
        )
    ),

    SCALE(
        mutableListOf(
            ActionType.SCALE_UP,
            ActionType.SCALE_DOWN
        )
    ),

    VM(
        mutableListOf(
            ActionType.CREATE_VM,
            ActionType.DELETE_VM
        )
    )
}
