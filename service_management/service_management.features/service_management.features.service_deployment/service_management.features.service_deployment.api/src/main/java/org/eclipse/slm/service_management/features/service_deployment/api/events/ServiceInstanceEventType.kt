package org.eclipse.slm.service_management.features.service_deployment.api.events

import org.eclipse.slm.common.messaging.MessageEventType

enum class ServiceInstanceEventType : MessageEventType {

    CREATED,
    UPDATED,
    DELETED

}