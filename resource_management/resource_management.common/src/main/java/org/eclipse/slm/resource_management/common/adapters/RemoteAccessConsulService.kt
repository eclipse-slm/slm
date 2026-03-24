package org.eclipse.slm.resource_management.common.adapters

import org.eclipse.slm.common.consul.model.catalog.NodeService
import org.eclipse.slm.common.consul.model.catalog.Service
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType
import org.eclipse.slm.resource_management.common.remote_access.RemoteAccessCreateDTO
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID

class RemoteAccessConsulService private constructor(
    id: UUID,
    var connectionType: ConnectionType,
    var credentialId: UUID,
    var username: String? = null
) : NodeService(id.toString()) {

    constructor(
        id: UUID,
        remoteAccess: RemoteAccessCreateDTO
    ) : this(id, remoteAccess.connectionType, remoteAccess.credentialId, remoteAccess.username) {
        super.port = remoteAccess.connectionPort
    }

    companion object {
        const val CONNECTION_TYPE_META_DATA_KEY = "connectionType"
        const val CREDENTIAL_ID_META_DATA_KEY = "credentialId"
        const val USERNAME_META_DATA_KEY = "username"

        @JvmStatic
        fun createFromService(service: Service): RemoteAccessConsulService {
            val meta = service.serviceMeta
            val connectionType = ConnectionType.valueOf(meta[CONNECTION_TYPE_META_DATA_KEY] ?: ConnectionType.ssh.name)
            val credentialId = UUID.fromString(meta[CREDENTIAL_ID_META_DATA_KEY]
                ?: throw IllegalArgumentException("Credential ID missing in service meta"))
            var username: String? = null;
            if (meta.containsKey(USERNAME_META_DATA_KEY)) {
                username = meta[USERNAME_META_DATA_KEY]
            }

            val remoteAccessConsulService = RemoteAccessConsulService(service.serviceId!!, connectionType, credentialId, username)
            remoteAccessConsulService.port = service.servicePort

            return remoteAccessConsulService
        }

        @JvmStatic
        fun createFromNodeService(nodeService: NodeService): RemoteAccessConsulService {
            val meta = nodeService.meta!!
            val connectionType = ConnectionType.valueOf(meta[CONNECTION_TYPE_META_DATA_KEY] ?: ConnectionType.ssh.name)
            val credentialId = UUID.fromString(meta[CREDENTIAL_ID_META_DATA_KEY]
                ?: throw IllegalArgumentException("Credential ID missing in service meta"))
            var username: String? = null;
            if (meta.containsKey(USERNAME_META_DATA_KEY)) {
                username = meta[USERNAME_META_DATA_KEY]
            }

            var remoteAccessServiceId = UUID.fromString(nodeService.id)
            val remoteAccessConsulService = RemoteAccessConsulService(remoteAccessServiceId, connectionType, credentialId, username)
            remoteAccessConsulService.port = nodeService.port

            return remoteAccessConsulService
        }

        @JvmStatic
        fun convertIdToServiceName(id: UUID, connectionType: ConnectionType): String {
            return "${connectionType.getConnectionTypeLoweredWithoutWhiteSpaces()}_${id}"
        }
    }

    var serviceId: UUID
        get() = UUID.fromString(super.id)
        set(value) {
            super.id = value.toString()
        }

    override var serviceName: String = ""
        get() = convertIdToServiceName(this.serviceId, this.connectionType)

    override var tags: List<String> = ArrayList()
        get() = arrayListOf(
            connectionType?.name ?: "",
            this.javaClass.simpleName
        )

    override var meta: Map<String, String> = HashMap()
        get() {
            val meta = hashMapOf(
                CONNECTION_TYPE_META_DATA_KEY to (connectionType?.name ?: ""),
                CREDENTIAL_ID_META_DATA_KEY to credentialId.toString()
            )
            username?.let { meta[USERNAME_META_DATA_KEY] = it }
            return meta
        }
}