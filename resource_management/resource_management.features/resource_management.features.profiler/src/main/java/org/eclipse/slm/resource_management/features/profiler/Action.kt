package org.eclipse.slm.resource_management.features.profiler

import com.fasterxml.jackson.annotation.*
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "actionClass")
@JsonSubTypes(
    JsonSubTypes.Type(value = AwxAction::class, name = "AwxAction"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class Action(actionClass: String) {

    open var actionClass: String = actionClass

    open var actionType: ActionType? = null

    open var skipable: Boolean = false

    open var configParameters: List<ActionConfigParameter> = emptyList()

    open var connectionTypes: MutableSet<ConnectionType> = HashSet()
}
