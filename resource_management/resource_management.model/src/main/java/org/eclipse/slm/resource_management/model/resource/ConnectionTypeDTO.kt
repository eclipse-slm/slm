package org.eclipse.slm.resource_management.model.resource

data class ConnectionTypeDTO(
    private val connectionType: ConnectionType
) {
    val name: String = connectionType.name
    val prettyName: String = connectionType.prettyName
    val defaultPort: Int? = connectionType.defaultPort
    val isRemote: Boolean = connectionType.remoteAccess
}
