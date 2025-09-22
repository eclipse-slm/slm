package org.eclipse.slm.service_management.service.rest.service_instances

import org.eclipse.slm.common.messaging.MessageEventType

enum class ServiceInstanceEventType : MessageEventType {

    CREATED,
    UPDATED,
    DELETED

}