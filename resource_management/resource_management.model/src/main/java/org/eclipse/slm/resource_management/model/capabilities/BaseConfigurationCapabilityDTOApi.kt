package org.eclipse.slm.resource_management.model.capabilities

import com.fasterxml.jackson.annotation.JsonTypeName
import org.eclipse.slm.resource_management.model.actions.Action
import org.eclipse.slm.resource_management.model.actions.ActionType
import org.eclipse.slm.resource_management.model.actions.AwxAction
import org.eclipse.slm.resource_management.model.actions.*
import java.util.*
import java.util.function.Consumer

@JsonTypeName("BaseConfiguration")
open class BaseConfigurationCapabilityDTOApi(id: UUID? = null) : CapabilityDTOApi(id, "BaseConfigurationCapability") {

    fun setActions(
        repo: String,
        branch: String,
        actionTypes: List<ActionType>
    ) {
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
