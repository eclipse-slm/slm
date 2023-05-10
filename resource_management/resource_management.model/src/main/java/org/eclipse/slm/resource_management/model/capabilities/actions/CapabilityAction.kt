package org.eclipse.slm.resource_management.model.capabilities.actions

import com.fasterxml.jackson.annotation.*
import org.eclipse.slm.resource_management.model.resource.ConnectionType

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "capabilityActionClass")
@JsonSubTypes(
    JsonSubTypes.Type(value = AwxCapabilityAction::class, name = "AwxCapabilityAction"),
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class CapabilityAction(capabilityActionClass: String) {

    open var capabilityActionClass: String = capabilityActionClass

    open var capabilityActionType: CapabilityActionType? = null

    open var skipable: Boolean = false

    open var configParameters: List<CapabilityActionConfigParameter> = emptyList()

    open var connectionTypes: MutableSet<ConnectionType> = HashSet()
}
