package org.eclipse.slm.resource_management.common.resources

import org.eclipse.slm.common.messaging.MessageEventType

enum class ResourceEventType : MessageEventType {

    CREATED,
    UPDATED,
    DELETED,

}