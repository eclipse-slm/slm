package org.eclipse.slm.resource_management.features.capabilities.dto

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.resource_management.features.capabilities.model.actions.Action
import org.eclipse.slm.resource_management.features.capabilities.model.actions.ActionType
import org.eclipse.slm.resource_management.features.capabilities.model.awx.AwxAction
import java.util.*
import java.util.function.Consumer

@JsonTypeName("VirtualizationCapability")
class VirtualizationCapabilityDTOApi(id: UUID? = null) : CapabilityDTOApi(id, "VirtualizationCapability") {

    fun setActions(repo: String, branch: String, actionTypes: List<ActionType>) {
        val actions = HashMap<ActionType, Action>()

        actionTypes.forEach(
            Consumer { t: ActionType ->
                actions[t] = AwxAction(
                    repo,
                    branch,
                    t.name + ".yml"
                )
            }
        )

        this.actions = actions
    }
}
