package org.eclipse.slm.notification_service.service.app.messaging.aas

import io.swagger.v3.oas.annotations.media.Schema
import org.eclipse.slm.notification_service.model.EventType
import org.eclipse.slm.notification_service.service.app.messaging.EventNotification
import java.util.*

class AasEventNotification(

    userId: String,

    timestamp: Date,

    override val eventType: AasEventType,

    val elementKind: AasElementKind,

    val resourceId: UUID,

    val shellId: String,

    val submodelId: String?,

    val submodelIdShort: String?,

    val semanticId: String?,

    ) : EventNotification(userId, timestamp) {
}

@Schema
enum class AasEventType : EventType {
    CREATED,
    UPDATED,
    DELETED
}

@Schema
enum class AasElementKind {
    SHELL,
    SUBMODEL,
    SUBMODEL_REFERENCE
}
