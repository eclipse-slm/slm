package org.eclipse.slm.resource_management.model.resource

import org.eclipse.slm.common.consul.model.catalog.CatalogNode
import org.eclipse.slm.common.consul.model.catalog.NodeService
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RemoteAccessService() : CatalogNode.Service() {
    constructor(connectionType: ConnectionType, credential: Credential?) : this() {
        this.connectionType = connectionType
        this.credential = credential
    }
    constructor(
        connectionType: ConnectionType,
        connectionPort: Int,
        credential: Credential?
    ) : this(connectionType, credential) {
        super.port = connectionPort
    }

    constructor(
        nodeService: NodeService,
        connectionType: ConnectionType,
        credential: Credential?
    ): this(
        connectionType,
        nodeService.Port,
        credential
    ) {
        super.service = nodeService.Service;
        super.id = UUID.fromString(nodeService.ID)
        super.address = nodeService.Address
    }

    var credential: Credential? = null
    var connectionType: ConnectionType? = null

    override var service: String? = null
        get() = getConnectionTypeLoweredWithoutWhiteSpaces()+"_"+id

    override var tags: List<String> = ArrayList()
        get() = arrayListOf(
            connectionType?.name ?: "",
            this.javaClass.simpleName
        )

    override var serviceMeta: Map<String, String> = HashMap()
        get() = hashMapOf(
            "connectionType" to (connectionType?.name ?: ""),
            "serviceClass" to javaClass.simpleName,
        )

    private fun getConnectionTypeLoweredWithoutWhiteSpaces(): String {
        return connectionType?.name?.lowercase()?.replace(" ","_") ?: ""
    }
}
